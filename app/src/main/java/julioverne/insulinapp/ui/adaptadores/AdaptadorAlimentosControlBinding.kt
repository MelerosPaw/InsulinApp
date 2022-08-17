@file:JvmName("AdaptadorAlimentosControlBinding")

package julioverne.insulinapp.ui.adaptadores

import julioverne.insulinapp.utils.DecimalFormatUtils

fun getCantidad(amount: Float): String = if (amount != 0f) {
    DecimalFormatUtils.decimalToStringIfZero(amount, 2, ".", ",")
} else {
    ""
}
