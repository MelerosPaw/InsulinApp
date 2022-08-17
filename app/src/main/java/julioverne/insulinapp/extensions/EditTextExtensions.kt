@file:JvmName("EditTextEX")

package julioverne.insulinapp.extensions

import android.widget.EditText

/** If the text is empty or blank, null will be returned */
var EditText.value: String?
    get() = text.takeIf { !it.isNullOrBlank() }?.toString()
    set(value) = setText(value)