package julioverne.insulinapp.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class ViewModelPeriodos : ViewModel() {

    val horaInicio: MutableLiveData<Calendar> = MutableLiveData()
    val horaFin: MutableLiveData<Calendar> = MutableLiveData()

    fun hayHoraInicio() = horaInicio != null
}