package julioverne.insulinapp.ui.listeners

import android.os.Bundle
import julioverne.insulinapp.data.dao.AlimentoDAO
import julioverne.insulinapp.ui.activities.FoodListFragment
import java.lang.ref.WeakReference

class OnSelectFoodListener(fragment: FoodListFragment) {

    private val fragmentWR: WeakReference<FoodListFragment> = WeakReference(fragment)

    fun devolverAlimentos(foodList: List<AlimentoDAO>) {
        fragmentWR.get()?.run {
            val bundle = Bundle()
            bundle.putParcelableArrayList(FoodListFragment.PARAM_SELECTED_FOOD, ArrayList(foodList))
            parentFragmentManager.setFragmentResult(FoodListFragment.KEY_SELECT_FOOD, bundle)
            goBack()
        }
    }
}