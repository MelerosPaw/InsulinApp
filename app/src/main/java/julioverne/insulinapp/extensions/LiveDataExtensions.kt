package julioverne.insulinapp.extensions

import julioverne.insulinapp.data.Status
import julioverne.insulinapp.ui.base.ResourceLiveData

inline fun <V, reified T : ResourceLiveData<V>> T.attempt(execution: (liveData: T) -> Unit) {
    if (value?.status != Status.LOADING) {
        loading()
        execution(this)
    }
}