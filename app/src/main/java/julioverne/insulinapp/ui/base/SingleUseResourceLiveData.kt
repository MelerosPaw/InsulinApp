package julioverne.insulinapp.ui.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import julioverne.insulinapp.data.Resource

class SingleUseResourceLiveData<T> : ResourceLiveData<T>() {

    private val oneTimeObservers: MutableSet<OneTimeResourceObserver<T>> = LinkedHashSet()

    override fun setValue(value: Resource<T>?) {
        super.setValue(value)

        if (value?.isSuccessful() == true) {
            super.setValue(null)
        }
    }

    override fun postValue(value: Resource<T>?) {
        super.postValue(value)

        oneTimeObservers.forEach { it.considerNotifying(value, this) }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in Resource<T>>) {
        if (observer is OneTimeResourceObserver<*>) {
            oneTimeObservers.add(observer as OneTimeResourceObserver<T>)
        }

        super.observe(owner, observer)
    }
}