package julioverne.insulinapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import julioverne.insulinapp.data.DataManager
import julioverne.insulinapp.data.Resource
import julioverne.insulinapp.data.dao.EntradaDAO

class DiarioViewModel(app: Application) : AndroidViewModel(app) {

    private val dataManager = DataManager.getInstance(app)
    private val _diaryLiveData: MutableLiveData<Resource<List<EntradaDAO>>> = MutableLiveData()

    val diaryLiveData: LiveData<Resource<List<EntradaDAO>>>
        get() = _diaryLiveData
    var selectedEntry: EntradaDAO? = null

    fun requestDiary() {
        _diaryLiveData.value = Resource.loading()
        _diaryLiveData.value = Resource.success(dataManager.entradas)
    }
}