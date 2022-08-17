package julioverne.insulinapp.ui.listeners;

import androidx.annotation.NonNull;
import julioverne.insulinapp.widgets.SimpleTextWatcher;

public class SearchListener extends SimpleTextWatcher {

    @NonNull
    private final FoodFilter foodFilter;

    public SearchListener(@NonNull final FoodFilter foodFilter) {
        this.foodFilter = foodFilter;
    }

    /**
     * Si no hay nada escrito, se muestran todos los listaAlimentos. Si se ha escrito algo,
     * se filtra la búsqueda por el nombre del alimento o la unidad de medida.
     */
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        foodFilter.filterFood(charSequence);
    }
}
