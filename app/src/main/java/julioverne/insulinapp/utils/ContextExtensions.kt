@file:JvmName("ContextExtensions")

package julioverne.insulinapp.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

@JvmOverloads
fun Context.toast(mensaje: String, duracion: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, mensaje, duracion).show()
}

@JvmOverloads
fun Context.toast(@StringRes stringId: Int, duracion: Int = Toast.LENGTH_SHORT) {
    toast(getString(stringId), duracion)
}