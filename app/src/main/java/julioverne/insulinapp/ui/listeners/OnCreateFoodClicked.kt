package julioverne.insulinapp.ui.listeners

import android.content.Context
import android.view.View
import julioverne.insulinapp.R
import julioverne.insulinapp.constants.Constants
import julioverne.insulinapp.data.DataManager
import julioverne.insulinapp.data.DataManager.OrigenImagen
import julioverne.insulinapp.utils.IntentUtil
import julioverne.insulinapp.utils.IntentUtil.IntentType
import julioverne.insulinapp.utils.toast

class OnCreateFoodClicked(private val dataManager: DataManager) : View.OnClickListener {

    override fun onClick(view: View?) {
        if (view != null) {
            dataManager.seleccionarOrigenImagen(view) { imageOrigin ->
                val intentType: IntentType = IntentType.CAMERA.takeIf {
                    imageOrigin == OrigenImagen.DESDE_CAMARA
                } ?: IntentType.FILE_SELECT
                launchIntent(view.context, intentType)
            }
        }
    }

    /**
     * Inicia un intent para obtener la imagen desde la camara o desde el sistema de archivos.
     */
    private fun launchIntent(context: Context, intentType: IntentType) {
        val requestCode = Constants.CAMERA_INTENT_REQUEST_CODE.takeIf {
            intentType == IntentType.CAMERA
        } ?: Constants.FILE_SEARCH_INTENT_REQUEST_CODE
        val intent = IntentUtil.getIntent(context, intentType)

        if (intent != null) {
            when (intentType) {
                IntentType.CAMERA -> {
                    context.toast(R.string.advertencia_foto)
//                    startActivityForResult(intent, requestCode) // TODO Juan Jose Melero 13/11/2021 Iniciar activity
                }
                IntentType.FILE_SELECT -> {
                } /*startActivityForResult(
                    Intent.createChooser(intent, "Selecciona una imagen..."),
                    requestCode)*/ // TODO: 13/11/2021 Lanzar intent
            }
        } else {
            when (intentType) {
                IntentType.CAMERA -> context.toast(R.string.advertencia_sin_camara)
                IntentType.FILE_SELECT -> context.toast(R.string.advertencia_sin_galeria)
            }
        }
    }
}