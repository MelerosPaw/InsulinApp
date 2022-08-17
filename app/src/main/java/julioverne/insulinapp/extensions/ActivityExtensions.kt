@file:JvmName("ActivityExtensions")

package julioverne.insulinapp.extensions

import android.app.Activity
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : ViewDataBinding> Activity.bind(@LayoutRes layoutId: Int): T =
    DataBindingUtil.setContentView(this, layoutId)
