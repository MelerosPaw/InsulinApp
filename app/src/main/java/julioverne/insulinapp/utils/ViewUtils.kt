@file:JvmName("ViewUtils")

package julioverne.insulinapp.utils

import android.view.View
import androidx.core.view.isVisible

fun setVisible(visible: Boolean, vararg views: View) {
    views.onEach { it.isVisible = visible }
}