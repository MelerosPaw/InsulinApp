@file:JvmName("FragmentExtensions")

package julioverne.insulinapp.extensions

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun <T> Fragment.getInvokingClassAs(clazz: Class<T>): T? = when {
    clazz.isInstance(targetFragment) -> clazz.cast(targetFragment)
    clazz.isInstance(parentFragment) -> clazz.cast(parentFragment)
    clazz.isInstance(activity) -> clazz.cast(activity)
    else -> null
}

@JvmOverloads
fun Fragment.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), text, duration).show()
}

@JvmOverloads
fun Fragment.toast(@StringRes stringId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), stringId, duration).show()
}

fun Fragment.longToast(text: String) {
    Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
}

fun Fragment.longToast(@StringRes textResId: Int) {
    Toast.makeText(requireContext(), textResId, Toast.LENGTH_LONG).show()
}

