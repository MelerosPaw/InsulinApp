package julioverne.insulinapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import julioverne.insulinapp.R
import julioverne.insulinapp.bo.EditFoodBO
import julioverne.insulinapp.data.DataManager
import julioverne.insulinapp.data.DataManager.GetAlimentosCallback
import julioverne.insulinapp.data.Resource
import julioverne.insulinapp.data.dao.AlimentoDAO
import julioverne.insulinapp.ui.activities.FoodListFragment
import julioverne.insulinapp.ui.base.ResourceLiveData
import julioverne.insulinapp.ui.base.SingleUseResourceLiveData
import julioverne.insulinapp.ui.listeners.FoodFilter
import kotlinx.coroutines.*

class AlimentosViewModel(val app: Application) : AndroidViewModel(app), FoodFilter {

  private val _foodListLiveData: ResourceLiveData<List<AlimentoDAO>> = ResourceLiveData()
  private val _foodDeleteLiveData: ResourceLiveData<List<AlimentoDAO>> = ResourceLiveData()
  private val _foodImageLoadedLiveData: SingleUseResourceLiveData<EditFoodBO> = SingleUseResourceLiveData()
  private val dataManager = DataManager.getInstance(app)
  private var foodList: List<AlimentoDAO>? = null
  private var filterJob: Job? = null

  val foodListLiveData: LiveData<Resource<List<AlimentoDAO>>>
    get() = _foodListLiveData
  val foodDeleteLiveData: LiveData<Resource<List<AlimentoDAO>>>
    get() = _foodDeleteLiveData
  val foodImageLoadedLiveData: LiveData<Resource<EditFoodBO>>
    get() = _foodImageLoadedLiveData
  var isListFiltered: Boolean = false

  fun requestFood(update: Boolean) {
//        showLoading(true);
//        if (actualizar) {
//            dataManager.getAlimentos(DataManager.DataSource.SERVER,
//                    new DataManager.GetAlimentosCallback() {
//                @Override
//                public void onSuccess(List<AlimentoDAO> alimentos) {
//                    adaptadorAlimentos.addAlimentos(alimentos);
//                    if (!alimentos.isEmpty() && adaptadorAlimentos.estaFiltrado()) {
//                        adaptadorAlimentos.filtrar(binding.etNombreAlimento.getText().toString());
//                    }
//                    showLoading(false);
//                }
//
//                @Override
//                public void onFailure() {
//                    FragmentExtensions.toast(his, "Error al intentar conectarse al servidor. Prueba otra vez más tarde.");
//                    showLoading(false);
//                }
//            });
//        } else {
//            dataManager.getAllAlimentos(new DataManager.GetAlimentosCallback() {
//                @Override
//                public void onSuccess(List<AlimentoDAO> alimentos) {
//                    if (alimentos == null) {
//                        StringUtils.toastLargo(AlimentosActivity.this, R.string.advertencia_recargar_alimentos);
//                        AlimentosActivity.super.onBackPressed();
//                    } else {
//                        cargarAlimentos(alimentos);
//                    }
//                    showLoading(false);
//                }
//
//                @Override
//                public void onFailure() {
//                    StringUtils.toastLargo(AlimentosActivity.this, R.string.problema_al_obtener_alimentos);
//                    AlimentosActivity.super.onBackPressed();
//                    showLoading(false);
//                }
//            });
//        }
    dataManager.getAlimentos(DataManager.DataSource.DATABASE,
      object : GetAlimentosCallback {
        override fun onSuccess(foodList: List<AlimentoDAO>) {
          this@AlimentosViewModel.foodList = foodList
          _foodListLiveData.success(foodList)
        }

        override fun onFailure() {
          this@AlimentosViewModel.foodList = null
          _foodListLiveData.error()
        }
      })
  }

  fun cancelFoodRequest() {
    dataManager.cancelarPeticionAlimentos()
  }

  fun deleteFoods(alimentos: List<AlimentoDAO>) {
    _foodDeleteLiveData.loading()

    val size = alimentos.size
    val eliminados = dataManager.eliminarAlimentos(alimentos)

    // Si ha fallado la transacción, no se ha borrado nada
    if (eliminados < size) {
      val errorMessage = app.getString(R.string.advertencia_imposible_eliminar);
      _foodDeleteLiveData.error(errorMessage)
    } else {

      // Borra las imágenes de todos los alimentos
      var imagenesBorradas = 0
      for (a in alimentos) {
        val borrada = dataManager.eliminarImagen(a.imagen)
        imagenesBorradas = if (borrada) imagenesBorradas + 1 else imagenesBorradas
      }
      Log.i(
        FoodListFragment.TAG, String.format(
          app.getString(R.string.advertencia_x_imagenes_borradas),
          imagenesBorradas
        )
      )
      _foodDeleteLiveData.success(alimentos)
    }
  }

  fun retrieveFoodImage(food: AlimentoDAO, position: Int) {
    _foodImageLoadedLiveData.loading()

    viewModelScope.launch(Dispatchers.IO) {
      dataManager.getBitmapFromMemory(food.imagen)
        ?.let { _foodImageLoadedLiveData.postSuccess(EditFoodBO(food, it, position)) }
        ?: _foodImageLoadedLiveData.postError()
    }


  }

  // region FoodFilter interface
  override fun filterFood(searchTerm: CharSequence?) {
    foodList?.let { foodList ->
      _foodListLiveData.loading()

      filterJob?.cancel()
      filterJob = viewModelScope.launch(Dispatchers.Default) {
        searchTerm?.toString().orEmpty()
          .takeIf(CharSequence::isNotBlank)
          ?.filterList(foodList)
          ?: restoreFoodList(foodList)
      }
    }
  }

  private suspend fun String.filterList(foodList: List<AlimentoDAO>) {
    isListFiltered = false
    val filtered = filterFoodList(foodList, this)
    _foodListLiveData.postSuccess(filtered)
  }

  private suspend fun filterFoodList(foodList: List<AlimentoDAO>, term: String): List<AlimentoDAO> =
    coroutineScope {
      foodList.filter {
        ensureActive()
        it.matchesNameOrMeasurementUnit(term)
      }
    }

  private fun restoreFoodList(foodList: List<AlimentoDAO>) {
    isListFiltered = false
    _foodListLiveData.postSuccess(foodList)
  }
  // endregion
}
