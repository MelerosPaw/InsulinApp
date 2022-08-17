package julioverne.insulinapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import julioverne.insulinapp.data.DataManager
import julioverne.insulinapp.data.DataManager.GetNombresAlimentosCallback
import julioverne.insulinapp.data.Resource
import julioverne.insulinapp.data.dao.ItemAEntrada
import julioverne.insulinapp.extensions.attempt
import julioverne.insulinapp.ui.base.ResourceLiveData
import julioverne.insulinapp.utils.isTrue

class FoodToBeEatenViewModel(app: Application) : AndroidViewModel(app) {

    private val _foodNamesLiveData = ResourceLiveData<List<String>>()
    private val dataManager = DataManager.getInstance(getApplication())
    private var postprandial: Boolean? = null
    private var selectedFood: List<ItemAEntrada>? = null
    val foodNamesLiveData: ResourceLiveData<List<String>>
        get() = _foodNamesLiveData
    var foodNames: MutableList<String>? = null

    fun initialize(postprandial: Boolean, selectedFood: List<ItemAEntrada>) {
        this.postprandial = postprandial
        this.selectedFood = selectedFood
    }

    fun requestFoodNames(): LiveData<Resource<List<String>>> {
        _foodNamesLiveData.attempt { liveData ->
            dataManager.getNombresAlimentos(object : GetNombresAlimentosCallback {
                override fun onSuccess(nombres: List<String>) {
                    liveData.success(nombres)
                }

                override fun onFailure() {
                    liveData.error()
                }
            })
        }

        return foodNamesLiveData
    }

    fun isPostprandial(): Boolean = postprandial.isTrue()

    fun getSelectedFood(): List<ItemAEntrada> = selectedFood.orEmpty()

    fun removeFoodName(name: String) {
        foodNames?.remove(name)
    }

    fun addFoodName(name: String) {
        foodNames?.add(name)
    }
}