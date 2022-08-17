@file:JvmName("ViewExtensions")

package julioverne.insulinapp.utils

import android.view.View
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@UseExperimental(ExperimentalContracts::class)
fun View?.visible(visible: Boolean) {
    contract {
        returns(true) implies (this@visible != null)
    }

    if (this != null) {
        visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

@UseExperimental(ExperimentalContracts::class)
fun View?.invisible(visible: Boolean) {

    contract {
        returns(true) implies (this@invisible != null)
    }

    if (this != null) {
        visibility = if (visible) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }
}

fun visible(visible: Boolean, vararg views: View?) {
    for (view in views) {
        view.visible(visible)
    }
}

fun invisible(invisible: Boolean, vararg views: View?) {
    for (view in views) {
        view.invisible(invisible)
    }
}
