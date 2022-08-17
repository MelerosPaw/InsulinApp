package julioverne.insulinapp.ui.base

import androidx.lifecycle.Observer
import julioverne.insulinapp.data.Resource
import julioverne.insulinapp.data.Status

abstract class OneTimeResourceObserver<T> : Observer<Resource<T>> {

    fun considerNotifying(t: Resource<T>?, liveData: ResourceLiveData<T>) {
        if (t != null && t.status != Status.LOADING) {
            onChanged(t)
            liveData.removeObserver(this)
        }
    }

}