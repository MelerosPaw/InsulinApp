package julioverne.insulinapp.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.ui.adaptadores.AdaptadorEntradas;
import julioverne.insulinapp.ui.dialogs.DialogDetalleEntrada;

public class DiarioActivity extends BaseActivity {

  @BindView(R.id.rv_diario)
  RecyclerView rvDiario;
  @BindView(R.id.tv_sin_entradas)
  TextView tvSinEntradas;
  @BindView(R.id.tv_titulo_fecha)
  TextView tvTituloFecha;
  @BindView(R.id.tv_titulo_glucosa_sangre)
  TextView tvTituloGlucosaSangre;
  @BindView(R.id.tv_titulo_dosis)
  TextView tvTituloDosis;

  private AdaptadorEntradas adaptadorEntradas;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.diario_activity_layout);
    ButterKnife.bind(this);
    loadView();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void loadView() {
    //cargarTipografiaCabecera();

    //Tipografía
    tvTituloFecha.setTypeface(negrita);
    tvTituloGlucosaSangre.setTypeface(negrita);
    tvTituloDosis.setTypeface(negrita);
    tvSinEntradas.setTypeface(negrita);

    if (!dataManager.isDiarioEmpty()) {
      tvSinEntradas.setVisibility(View.INVISIBLE);
      adaptadorEntradas = new AdaptadorEntradas(this, dataManager.getEntradas());
      rvDiario.setAdapter(adaptadorEntradas);
      adaptadorEntradas.setOnItemClickListener(new AdaptadorEntradas.OnItemClickListener() {
        @Override
        public void onItemClicked(EntradaDAO entrada, int posicion) {
          mostrarDialogAlimento(entrada);
        }
      });
      rvDiario.setVisibility(View.VISIBLE);
    } else {
      rvDiario.setVisibility(View.INVISIBLE);
      tvSinEntradas.setVisibility(View.VISIBLE);
    }
  }

  private void mostrarDialogAlimento(EntradaDAO entrada) {
    DialogDetalleEntrada dialog = DialogDetalleEntrada.newInstance(entrada);
    dialog.setOnCloseListener(new DialogDetalleEntrada.OnCloseListener() {
      @Override
      public void onClose() {
        adaptadorEntradas.deseleccionarItem();
      }
    });
    dialog.show(getSupportFragmentManager(), DialogDetalleEntrada.TAG);
  }

  @Override
  protected void onResume() {
    if (!dataManager.isDiarioEmpty()) {
      adaptadorEntradas.notifyDataSetChanged();
    }
    super.onResume();
  }
}
