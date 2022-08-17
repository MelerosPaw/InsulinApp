@file:JvmName("MessageExtensions")

package julioverne.insulinapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

fun Activity.showAlert(text: String, buttonText: String): AlertDialog {
    val dialog = createAlertDialog(this, text, buttonText)
    dialog.show()
    return dialog
}

fun Context.showAlert(text: String, buttonText: String): AlertDialog {
    val dialog = createAlertDialog(this, text, buttonText)
    dialog.show()
    return dialog
}

fun Activity.showAlert(
    text: String, buttonText: String,
    dismissListener: DialogInterface.OnDismissListener
): AlertDialog {
    val dialog = createAlertDialog(this, text, buttonText)
    dialog.setOnDismissListener(dismissListener)
    dialog.show()
    return dialog
}

private fun createAlertDialog(context: Context, text: String? = null, buttonText: String) =
    AlertDialog.Builder(context)
        .setTitle("Atención")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage(text)
        .setPositiveButton(buttonText) { dialog, _ -> dialog.dismiss() }
        .create()