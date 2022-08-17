package julioverne.insulinapp.ui.listeners

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import julioverne.insulinapp.R
import julioverne.insulinapp.data.dao.AlimentoDAO

class OnDeleteFoodListener(private val onDeleteClickedListener: OnDeleteClickedListener) {

    interface OnDeleteClickedListener {
        fun onDelete(foods: List<AlimentoDAO>)
    }

    fun mostrarDialogEliminacion(
        context: Context,
        alimentos: Array<List<AlimentoDAO>>
    ) {
        val alimentosUsuario = alimentos[1]
        val thereIsAnyUserDefinedFood = alimentosUsuario.isNotEmpty()
        val mensaje = getMessage(context, alimentosUsuario, thereIsAnyUserDefinedFood)
        val positiveListener = DialogInterface.OnClickListener { _, _ ->
            onDeleteClickedListener.onDelete(alimentosUsuario)
        }

        // Si solo se han escogido alimentos del sistema, solo se muestra un aviso
        // Si solo hay alimentos de usuario, se le indican los alimentos que se van a eliminar
        // Si hay alimentos de los dos tipos, se le avisa de cuáles se van a eliminar
        @StringRes
        val botonPositivo =
            if (thereIsAnyUserDefinedFood) R.string.dialog_borrar else R.string.dialog_entendido

        val dialogoEliminar = AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title)
            .setMessage(mensaje)
            .setPositiveButton(botonPositivo, positiveListener)

        //Si solo se han escogido alimentos que no se pueden borrar, no muestra el botón de cancelar
        if (thereIsAnyUserDefinedFood) {
            val negativeListener = DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }
            dialogoEliminar.setNegativeButton(R.string.cancelar, negativeListener)
        }

        dialogoEliminar.show()
    }

    private fun getMessage(
        context: Context,
        alimentosUsuario: List<AlimentoDAO>,
        thereIsNoUserDefinedFood: Boolean
    ): String = if (thereIsNoUserDefinedFood) {
        context.getString(R.string.dialog_sin_seleccion)
    } else {
        StringBuilder()
            .append(context.getString(R.string.dialog_message_se_eliminaran)).append("\n\n")
            .append(alimentosUsuario.joinToString { "- ${it.nombre}\n" })
            .append("\n")
            .append(context.getString(R.string.dialog_message_seguro))
            .toString()
    }
}