package io.keyss.library.common.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.keyss.library.common.BuildConfig
import kotlin.concurrent.thread
import kotlin.math.abs

/**
 * @author Key
 * Time: 2022/02/17 15:46
 * Description:
 */
class Camera1Preview : TextureView, LifecycleObserver {
    private val TAG = this::class.simpleName

    /**
     * 是否默认加载完就启动
     */
    var isDefaultStart = true

    // 空间是否已渲染完成
    private var isViewDrawFinished = false
    private var isPreviewing = false
    private var mLastPreviewStatus = false

    private var mLifecycleOwner: LifecycleOwner? = null
    val numberOfCameras: Int = Camera.getNumberOfCameras()

    private var mImageData: ByteArray? = null
    private var mImageDataTime: Long = 0

    // 控件的宽高
    private var viewHeight: Int = 0
    private var viewWidth: Int = 0

    // 摄像头参数
    // 摄像头默认方向，大概率是90
    private var mRotation: Int = 0

    // 视图显示方向
    private var mDisplayRotation: Int = getDisplayRotation()

    // 用户自定义 额外的旋转角度，用作多设备适配,加法，可以正负使用
    var extraRotation = 0

    // 摄像头为了配合屏幕尺寸，一般都是横向观看, 系统名词: LANDSCAPE横向，PORTRAIT纵向
    private var isLandscape = true

    // 手机的话，前置一般需要镜像一下
    var isMirror = false
    private var previewFormat = ImageFormat.NV21

    // real预览尺寸
    private var mPreviewWidth: Int = 0
    private var mPreviewHeight: Int = 0

    // 用户设定的期望值
    var expectedPreviewWidth: Int? = null
    var expectedPreviewHeight: Int? = null

    /**
     * 默认的摄像头ID，默认后置0
     * Camera.CameraInfo.CAMERA_FACING_BACK = 0
     * Camera.CameraInfo.CAMERA_FACING_FRONT = 1
     */
    var mCameraId = 0

    var mCamera: Camera? = null
    var onPreviewFrameListener: ((nv21: ByteArray, camera: Camera) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                isViewDrawFinished = true

            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "onSurfaceTextureSizeChanged() called with: surface = $surface, width = $width, height = $height")
                viewWidth = width
                viewHeight = height
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.d(TAG, "onSurfaceTextureDestroyed() called with: surface = $surface")
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }

    }

    @Throws
    fun startPreview(cameraID: Int = 0) {
        if (numberOfCameras < 1) {
            throw RuntimeException("没有检测到摄像头")
        }
        if (cameraID + 1 > numberOfCameras) {
            throw RuntimeException("没有检测到ID=${cameraID}的摄像头")
        }

        if (isPreviewing) {
            if (mCameraId == cameraID) {
                Log.w(TAG, "已开启，不重复开启")
                return
            } else {
                stopPreview()
            }
        }
        isPreviewing = true
        mCameraId = cameraID
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(mCameraId, cameraInfo)
        // 摄像头的默认角度
        mRotation = cameraInfo.orientation
        if (isViewDrawFinished) {
            startPreviewCore()
        } else {
            thread {
                // 一秒钟，一般都在1秒之内
                val startTimeMillis = System.currentTimeMillis()
                repeat(100) {
                    SystemClock.sleep(10)
                    if (isViewDrawFinished) {
                        post {
                            startPreviewCore()
                        }
                        Log.d(TAG, "startPreview() 等待了${System.currentTimeMillis() - startTimeMillis}ms, 渲染完成后打开摄像头")
                        return@thread
                    }
                }
            }
        }
    }

    /**
     * 权限未请求！
     */
    private fun startPreviewCore() {
        viewHeight = measuredHeight
        viewWidth = measuredWidth

        Log.i(
            TAG,
            "startPreviewCore, 控件宽高: viewHeight=${viewHeight}, viewWidth=${viewWidth}, CameraRotation=${mRotation}, DisplayRotation=${mDisplayRotation}"
        )
        mCamera = Camera.open(mCameraId).also {
            it.setPreviewTexture(surfaceTexture)

            val parameters = it.parameters
            // 先拿默认预览尺寸计算下横竖
            Log.d(
                TAG, "默认的尺寸: 高=${parameters.previewSize.height}, 宽=${parameters.previewSize.width}, " +
                        "期望得到的尺寸:  高=${expectedPreviewHeight}, 宽=${expectedPreviewWidth}"
            )

            isLandscape = parameters.previewSize.let { size ->
                size.width > size.height
            }

            // 理论上相机角度和屏幕应该是一致的，纵向屏幕,理论上摄像头和屏幕理论上应该成90度夹角
            // 相机角度默认等于屏幕角度 + 相机默认角度
            var rotation = mDisplayRotation + mRotation
            Log.i(TAG, "设定方向：rotation1=$rotation")
            val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            val angleDiff = abs(mRotation - mDisplayRotation)
            if (isPortrait && (angleDiff == 0 || angleDiff == 180)) {
                rotation += 90
            } else if (!isPortrait && (angleDiff == 90 || angleDiff == 270)) {
                rotation -= 90
            }
            Log.i(TAG, "设定方向：rotation2=$rotation")
            Log.i(TAG, "设定方向：rotation3=${rotation + extraRotation}")
            rotation = correctRotation(rotation + extraRotation)
            // 工控机很多装配不严谨，摄像头是装反的情况，就需要手动指定extraRotation参数
            Log.i(TAG, "最终方向：rotation=$rotation, isPortrait=$isPortrait")
            it.setDisplayOrientation(rotation)
            parameters.previewFormat = previewFormat

            // 设置预览尺寸，对比期望的预览尺寸得出正确的预览尺寸，todo 再用屏幕方向做一个旋转
            if (null != expectedPreviewHeight && null != expectedPreviewWidth) {
                mPreviewWidth = expectedPreviewWidth as Int
                mPreviewHeight = expectedPreviewHeight as Int
            } else {
                mPreviewWidth = viewWidth
                mPreviewHeight = viewHeight
            }
            // 正确时：横向：宽大于高，纵向：宽小于高
            if ((isLandscape && mPreviewWidth < mPreviewHeight) || (mPreviewWidth > mPreviewHeight)) {
                swapWidthHeight()
            }

            val bestSupportedSize = getBestSupportedSize(parameters.supportedPreviewSizes, mPreviewWidth, mPreviewHeight)
            mPreviewWidth = bestSupportedSize.width
            mPreviewHeight = bestSupportedSize.height
            parameters.setPreviewSize(mPreviewWidth, mPreviewHeight)
            Log.d(TAG, "支持的对焦模式有: ${parameters.supportedFocusModes}")
            if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                // 优先使用持续对焦
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
            it.parameters = parameters

            // 缓冲预览
            val size: Int = mPreviewWidth * mPreviewHeight * ImageFormat.getBitsPerPixel(previewFormat) / 8
            mImageData = ByteArray(size)
            it.addCallbackBuffer(mImageData)
            it.setPreviewCallbackWithBuffer { nv21: ByteArray, camera: Camera ->
                camera.addCallbackBuffer(nv21)
                // 使用时深拷贝一份
                onPreviewFrameListener?.invoke(nv21, camera)
            }
            if (isMirror) {
                scaleY = -1f
            }
            // debug模式下输出一下数据
            if (BuildConfig.DEBUG) {
                val fpsRange = IntArray(2)
                it.parameters.getPreviewFpsRange(fpsRange)
                Log.i(
                    TAG,
                    //"预览Size: ${it.parameters.get("preview-size")}, " +
                    "Real预览Size: 宽=${it.parameters.previewSize.width}, 高=${it.parameters.previewSize.height}, " +
                            "帧率: ${it.parameters.previewFrameRate}, " +
                            "Fps: ${fpsRange.contentToString()}, " +
                            "横向: $isLandscape, " +
                            "数据格式: ${it.parameters.previewFormat}, " +
                            "对焦模式: ${it.parameters.focusMode}, " +
                            "缓冲池尺寸: $size"
                )
            }
            // onCreate中直接启动会在未渲染出Texture导致无法预览
            it.startPreview()
        }
    }

    /**
     * 交换宽高
     */
    private fun swapWidthHeight() {
        mPreviewWidth = mPreviewWidth xor mPreviewHeight
        mPreviewHeight = mPreviewWidth xor mPreviewHeight
        mPreviewWidth = mPreviewWidth xor mPreviewHeight
    }

    fun stopPreview() {
        Log.i(TAG, "stopPreview")
        mCamera?.let {
            it.stopPreview()
            it.release()
        }
        mCamera = null
        isPreviewing = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (!isViewDrawFinished && isDefaultStart) {
            // 默认调用时只打印日志，手动调用可决定提示展示方式
            try {
                startPreview()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (mLastPreviewStatus) {
                resumePreview()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        mLastPreviewStatus = isPreviewing
        if (isPreviewing) {
            pausePreview()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        stopPreview()
    }


    fun pausePreview(): Unit {
        mCamera?.stopPreview()
    }


    fun resumePreview(): Unit {
        mCamera?.startPreview()
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        if (mLifecycleOwner === lifecycleOwner) {
            return
        }
        mLifecycleOwner?.lifecycle?.removeObserver(this)
        mLifecycleOwner = lifecycleOwner
        lifecycleOwner.lifecycle.addObserver(this)
    }

    // 1000/30=33.3，正常使用的摄像头1秒30帧
    fun getCurrentImageData(expirationDate: Long = 68): ByteArray? {
        if (System.currentTimeMillis() - mImageDataTime < expirationDate) {
            return mImageData?.clone()
        }
        return null
    }

    /**
     * 设置期望的预览值
     */
    fun setPreviewSize(width: Int, height: Int) {
        expectedPreviewHeight = height
        expectedPreviewWidth = width
    }

    /**
     * 获取真正的预览尺寸，在打开摄像头后才真正Real
     */
    fun getRealPreviewSize(): Array<Int> {
        return arrayOf(mPreviewHeight, mPreviewWidth)
    }

    private fun getBestSupportedSize(sizes: List<Camera.Size>?, width: Int, height: Int): Camera.Size {
        if (sizes.isNullOrEmpty()) {
            return mCamera!!.parameters.previewSize
        }
        // 拷贝一份，从小到大排序
        val sizeList: List<Camera.Size> = sizes.sortedByDescending {
            it.width
        }
        var bestSize = sizeList[0]
        var previewViewRatio: Float
        previewViewRatio = width.toFloat() / height.toFloat()
        if (previewViewRatio > 1) {
            previewViewRatio = 1 / previewViewRatio
        }
        val isNormalRotate = extraRotation % 180 == 0
        for (s in sizeList) {
            if (mPreviewWidth == s.width && mPreviewHeight == s.height) {
                return s
            }
            if (isNormalRotate) {
                if (abs(s.height / s.width.toFloat() - previewViewRatio) < abs(bestSize.height / bestSize.width.toFloat() - previewViewRatio)) {
                    bestSize = s
                }
            } else {
                if (abs(s.width / s.height.toFloat() - previewViewRatio) < abs(bestSize.width / bestSize.height.toFloat() - previewViewRatio)) {
                    bestSize = s
                }
            }
        }
        return bestSize
    }


    private fun getDisplayRotation(): Int {
        val rotation = ContextCompat.getSystemService(context, WindowManager::class.java)?.defaultDisplay?.rotation ?: 0
        return changeDisplayRotation(rotation)
    }

    // Surface.Rotation
    private fun changeDisplayRotation(rotation: Int): Int {
        return when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> rotation * 90
        }.let {
            correctRotation(it)
        }
    }

    private fun correctRotation(rotation: Int): Int {
        // 防超360
        return rotation / 90 * 90 % 360
    }


    fun getInfo(): String {
        return "总共有${numberOfCameras}个摄像头"
    }
}