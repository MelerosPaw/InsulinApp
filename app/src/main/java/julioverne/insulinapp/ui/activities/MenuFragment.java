package julioverne.insulinapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.constants.NavigationActions;
import julioverne.insulinapp.databinding.FragmentMenuLayoutBinding;
import julioverne.insulinapp.ui.adaptadores.OptionMenuAdapter;
import julioverne.insulinapp.ui.base.NavigationFragment;

/**
 * Created by Juan José Melero on 09/05/2015.
 */
public class MenuFragment extends NavigationFragment {

  //    public static void start(@NonNull final Context context) {
  //        final Intent intent = new Intent(context, MenuFragment.class);
  //        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
  //        context.startActivity(intent);
  //    }

  private FragmentMenuLayoutBinding binding;
  @NonNull
  private final AdapterView.OnItemClickListener onOptionClickedListener =
      new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
          final String elemento = (String) binding.lvOpcionesPrincipales.getItemAtPosition(position);

          switch (elemento) {
            case Constants.REALIZAR_CONTROL:
              abrirControlActivity(view.getContext());
              break;
            case Constants.DIARIO:
              navigateTo(NavigationActions.DIARY);
              break;
            case Constants.ALIMENTOS:
              navigateTo(NavigationActions.FOOD_LIST);
              break;
            case Constants.INSULINA_ACTUAL:
              navigateTo(NavigationActions.INSULINE_AMOUNT);
              break;
            case Constants.ALARMAS:
              view.getContext().startActivity(new Intent(AlarmClock.ACTION_SET_ALARM));
              break;
          }
        }
      };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    binding = FragmentMenuLayoutBinding.inflate(inflater, container, false);
    loadView(binding.getRoot().getContext());
    return binding.getRoot();
  }

  private void loadView(@NonNull final Context context) {
    //TODO Para actualizaciones conviene que compruebe cuántas imágenes hay guardadas
    //Guarda las imágenes por defecto en el móvil si no están guardadas ya
    if (!dataManager.isImagenesCargadas()) {
      dataManager.cargarImagenes();
    }

    setUpOptionList(context);
    dataManager.loadDefaultPreferences();
  }

  private void setUpOptionList(@NonNull final Context context) {
    final String[] opciones = getResources().getStringArray(R.array.opciones_main);
    final OptionMenuAdapter adaptadorOpciones = new OptionMenuAdapter(context,
        R.layout.item_opcion_simple_layout, opciones);
    binding.setOptionsAdapter(adaptadorOpciones);
    binding.setOnItemClickedListener(onOptionClickedListener);
  }

  private void abrirControlActivity(@NonNull final Context context) {
    if (ajustesEstablecidos()) {
      context.startActivity(new Intent(context, ControlActivity.class));
    }
  }

  private boolean ajustesEstablecidos() {
    if (!dataManager.arePreferencesSet()) {
      longToast(getString(R.string.advertencia_preferencias_sin_configurar));
      return false;
    }
    return true;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_main, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.copia_seguridad) {
      navigateTo(NavigationActions.BACK_UP);
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
}