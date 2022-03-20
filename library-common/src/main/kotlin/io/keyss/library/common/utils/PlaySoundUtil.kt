package io.keyss.library.common.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RawRes
import java.util.*

/**
 * @author Key
 * Time: 2019/11/22 09:52
 * Description:
 */
object PlaySoundUtil {
    private lateinit var mApplicationContext: Context
    private var mCurrentResID: Int? = null
    private val mMediaPlayer: MediaPlayer by lazy {
        MediaPlayer()
    }
    private val mList: Stack<Int> by lazy {
        Stack()
    }

    private var mSpeedUnableSet = false
    var defaultSpeed: Float = 1.0f
        set(value) {
            field = when {
                value > 2f -> 2f
                value < 0f -> 0f
                else -> value
            }
        }

    fun init(context: Context) {
        mApplicationContext = context.applicationContext
        mMediaPlayer.isLooping = false
        mMediaPlayer.setOnCompletionListener {
            println("playRawSound Completion ResID=${mCurrentResID}")
            mCurrentResID = null
            playNext()
        }
    }

    fun setSpeed(speed: Float? = null, isDefault: Boolean = false) {
        // 速度未改变，或已知的无法改变
        if (this.defaultSpeed == speed || mSpeedUnableSet) {
            return
        }
        // 相当于不设置速度
        if (null == speed && defaultSpeed == 1f) {
            return
        }
        if (isDefault && speed != null) {
            defaultSpeed = speed
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val playbackParams = mMediaPlayer.playbackParams
                playbackParams.speed = speed ?: this.defaultSpeed
                mMediaPlayer.playbackParams = playbackParams
                if (!mMediaPlayer.isPlaying) {
                    mMediaPlayer.pause()
                }
            } catch (e: Exception) {
                mSpeedUnableSet = true
                e.printStackTrace()
            }
        } else {
            mSpeedUnableSet = true
        }
    }

    fun playRawSound(isCutAndTop: Boolean, @RawRes vararg resId: Int) {
        if (isCutAndTop) {
            mList.clear()
            mMediaPlayer.stop()
        }
        mList.addAll(resId.asList())
        playNext()
    }

    /**
     * @param isExistSameOverwrite 存在相同资源是否重复添加
     * @param isCutAndTop 是否清除队列后顶上去
     */
    fun playRawSound(@RawRes resId: Int, isExistSameOverwrite: Boolean, isCutAndTop: Boolean, isCurrentReplay: Boolean) {
        println("playRawSound Add resId = [${resId}], isSameOverwrite = [${isExistSameOverwrite}], isCutAndTop = [${isCutAndTop}], isCurrentReplay = [${isCurrentReplay}]")
        if (isCutAndTop) {
            mList.clear()
            mList.add(resId)
            mMediaPlayer.stop()
            playNext()
            return
        }
        if (isCurrentReplay && resId == mCurrentResID) {
            // 重新播放当前音频
            mMediaPlayer.seekTo(0)
            return
        }
        // 队列的情况下才需要判断，否则直接切上去
        if (isExistSameOverwrite || !mList.contains(resId)) {
            // 当前没在播，并且设置了重复播放
            mList.add(resId)
        }
        playNext()
    }

    @Synchronized
    private fun playNext() {
        if (mList.empty() || mMediaPlayer.isPlaying) {
            println("playRawSound playNext size=${mList.size}, isPlaying=${mMediaPlayer.isPlaying}")
            return
        }
        try {
            println("playRawSound playNext start size=${mList.size}")
            val topRes = mList.pop()
            mCurrentResID = topRes
            mMediaPlayer.reset()
            val afd: AssetFileDescriptor = mApplicationContext.resources.openRawResourceFd(topRes)
            mMediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mMediaPlayer.prepare()
            setSpeed()
            mMediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
            mCurrentResID = null
            // 报错继续下一个，直接播完
            //playNext()
        }
    }
}
