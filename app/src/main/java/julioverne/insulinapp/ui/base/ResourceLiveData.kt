package julioverne.insulinapp.ui.base

import androidx.lifecycle.MutableLiveData
import julioverne.insulinapp.data.Resource

open class ResourceLiveData<T> : MutableLiveData<Resource<T>>() {

    fun loading() {
        value = Resource.loading()
    }

    @JvmOverloads
    fun error(message: String? = null) {
        value = Resource.error(message)
    }

    fun success(data: T) {
        value = Resource.success(data)
    }

    fun postSuccess(data: T) {
        postValue(Resource.success(data))
    }

    @JvmOverloads
    fun postError(message: String? = null) {
        postValue(Resource.error(message))
    }

    fun postLoading() {
        postValue(Resource.loading())
    }
}