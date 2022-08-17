@file:JvmName("DiaryAdapterBinding")

package julioverne.insulinapp.ui.adaptadores

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import julioverne.insulinapp.R
import julioverne.insulinapp.data.DataManager
import julioverne.insulinapp.data.SharedPreferencesManager
import julioverne.insulinapp.data.dao.EntradaDAO
import julioverne.insulinapp.utils.DateUtils

fun formatDate(entry: EntradaDAO): String = DateUtils.dateToString(entry.fecha)

fun formatTime(entry: EntradaDAO): String = DateUtils.dateToHour(entry.fecha)

//  Tiñe la fila de un color rojo si la cantidad de glucosa era incorrecta
fun getRowBackground(entry: EntradaDAO, context: Context): Drawable? {
    val dataManager = DataManager.getInstance(context)

    return ContextCompat.getDrawable(
        context, when {
            entry.glucosaSangre < dataManager.getPreferencia(SharedPreferencesManager.GLUCOSA_MINIMA).toInt() ->
                R.drawable.selector_degradado_inferior
            entry.glucosaSangre > dataManager.getPreferencia(SharedPreferencesManager.GLUCOSA_MAXIMA).toInt() ->
                R.drawable.selector_degradado_superior
            else -> R.drawable.selector_degradado_correcto
        }
    )
}

// Muestra la manzanita según si fue antes o después de comer
@DrawableRes
fun getPostPrandialIcon(entry: EntradaDAO): Int =
    if (entry.isAntesDeComer) {
        R.mipmap.ic_manzanita
    } else {
        R.mipmap.ic_manzanita_mordida
    }