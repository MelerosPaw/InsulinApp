package julioverne.insulinapp.ui.base

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

open class NavigationFragment : BaseFragment() {

    private val navigationController: NavController
        get() = NavHostFragment.findNavController(this)

    fun navigateTo(@IdRes actionId: Int) {
        navigationController.navigate(actionId)
    }

    open fun goBack() {
        navigationController.popBackStack()
    }

}