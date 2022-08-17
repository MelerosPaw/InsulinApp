package julioverne.insulinapp.bo

import android.graphics.Bitmap
import julioverne.insulinapp.data.dao.AlimentoDAO

data class EditFoodBO(
    val food: AlimentoDAO,
    val foodImage: Bitmap,
    val position: Int
)