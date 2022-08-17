@file:JvmName("ViewHolderEXT")

package julioverne.insulinapp.extensions

import android.content.Context
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

val RecyclerView.ViewHolder.context: Context
    get() = itemView.context

private val RecyclerView.ViewHolder.resources: Resources
    get() = context.resources

fun RecyclerView.ViewHolder.getString(@StringRes id: Int, vararg args: Any) = resources.getString(id, *args)

@JvmOverloads
fun RecyclerView.ViewHolder.getQuantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any = emptyArray()) =
    resources.getQuantityString(id, quantity, *args)


