package julioverne.insulinapp.ui.base

import androidx.lifecycle.MutableLiveData

class SingleUseLiveData<T> : MutableLiveData<T?>() {

    override fun setValue(value: T?) {
        super.setValue(value)
        super.setValue(null)
    }

    override fun postValue(value: T?) {
        super.postValue(value)
        super.postValue(null)
    }
}