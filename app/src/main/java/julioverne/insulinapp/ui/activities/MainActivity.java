package julioverne.insulinapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.Menu;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.ui.adaptadores.AdaptadorOpcionesMainActivity;
import julioverne.insulinapp.utils.StringUtils;

/**
 * Created by Juan José Melero on 09/05/2015.
 */
public class MainActivity extends BaseActivity {

  @BindView(R.id.lv_opcionesPrincipales)
  ListView lvOpcionesPrincipales;

  private AdaptadorOpcionesMainActivity adaptadorOpciones;
  private Unbinder unbinder;

  public static void start(Context context) {
    Intent intent = new Intent(context, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity_layout);
    unbinder = ButterKnife.bind(this);
    loadView();
  }

  @Override
  protected void loadView() {

    //        cargarTipografiaCabecera();

    //TODO Para actualizaciones conviene que compruebe cuántas imágenes hay guardadas
    //Guarda las imágenes por defecto en el móvil si no están guardadas ya
    if (!dataManager.isImagenesCargadas()) {
      dataManager.cargarImagenes();
    }

    String[] opciones = getResources().getStringArray(R.array.opciones_main);
    adaptadorOpciones = new AdaptadorOpcionesMainActivity(this,
        R.layout.item_opcion_simple_layout, R.id.tv_opcion, opciones);
    lvOpcionesPrincipales.setAdapter(adaptadorOpciones);
    dataManager.loadDefaultPreferences();
  }

  //Abre una Activity u otra según el ítem clicado
  @OnItemClick(R.id.lv_opcionesPrincipales)
  public void escogerOpcion(int position) {

    String elemento = (String) lvOpcionesPrincipales.getItemAtPosition(position);
    switch (elemento) {
      case Constants.REALIZAR_CONTROL:
        abrirControlActivity();
        break;
      case Constants.DIARIO:
        startActivity(new Intent(this, DiarioActivity.class));
        break;
      case Constants.ALIMENTOS:
        startActivity(new Intent(this, AlimentosActivity.class));
        break;
      case Constants.INSULINA_ACTUAL:
        startActivity(new Intent(this, InsulinaActualActivity.class));
        break;
      case Constants.ALARMAS:
        startActivity(new Intent(AlarmClock.ACTION_SET_ALARM));
        break;
    }
  }

  private void abrirControlActivity() {
    if (ajustesEstablecidos()) {
      startActivity(new Intent(this, ControlActivity.class));
    }
  }

  private boolean ajustesEstablecidos() {
    if (!dataManager.arePreferencesSet()) {
      StringUtils.toastLargo(this, getString(R.string.advertencia_preferencias_sin_configurar));
      return false;
    }
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbinder.unbind();
  }
}