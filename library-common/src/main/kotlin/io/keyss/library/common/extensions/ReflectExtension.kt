package io.keyss.library.common.extensions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType


class ViewBindingError(message: String?) : Error(message)
/**
 * @author Key
 * Time: 2021/01/27 15:46
 * Description:
 */
@Throws(ViewBindingError::class)
fun <T : Any> T.getViewBindingGenericClass(): Class<*> {
    (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.forEach {
        if (ViewBinding::class.java.isAssignableFrom(it as Class<*>)) {
            return it
        }
    }
    throw ViewBindingError("没有找到ViewBinding的泛型")
}

@Throws(Exception::class)
fun <T : Any> T.getGenericClass(genericIndex: Int): Class<*> {
    return (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[genericIndex] as Class<*>
}

/**
 * 提高调用时的扩展性以及便利性
 */
@Throws(Throwable::class)
fun <T : Any> T.getViewBinding(
    inflater: LayoutInflater,
    parent: ViewGroup? = null,
    attachToParent: Boolean = false,
    genericIndex: Int = 0,
): ViewBinding {
    return if (genericIndex == 0) {
        getViewBindingGenericClass()
    } else {
        getGenericClass(genericIndex)
    }.inflateBinding(inflater, parent, attachToParent)
}

/**
 * @NonNull
 * public static FragmentCourseListBinding inflate(@NonNull LayoutInflater inflater) {
 *     return inflate(inflater, null, false);
 * }
 */
@Throws(Exception::class)
fun Class<*>.inflateBinding(inflater: LayoutInflater): ViewBinding {
    return getMethod("inflate", LayoutInflater::class.java)
        .invoke(null, inflater) as ViewBinding
}

/**
 * @NonNull
 * public static FragmentCourseListBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent) {
 *     View root = inflater.inflate(R.layout.fragment_course_list, parent, false);
 *     if (attachToParent) {
 *         parent.addView(root);
 *     }
 *     return bind(root);
 * }
 */
@Throws(Exception::class)
fun Class<*>.inflateBinding(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): ViewBinding {
    return getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        .invoke(null, inflater, parent, attachToParent) as ViewBinding
}