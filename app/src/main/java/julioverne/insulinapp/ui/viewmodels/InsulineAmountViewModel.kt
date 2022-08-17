package julioverne.insulinapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import julioverne.insulinapp.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InsulineAmountViewModel(app: Application) : AndroidViewModel(app) {

    private val _currentInsulineAmountLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentInsulineLiveData: LiveData<Float>
        get() = _currentInsulineAmountLiveData

    fun retrieveCurrentInsulineAmount() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentInsulineAmount: Float = DataManager.getInstance(getApplication()).calcularInsulinaRestante()
            _currentInsulineAmountLiveData.postValue(currentInsulineAmount)
        }
    }

}