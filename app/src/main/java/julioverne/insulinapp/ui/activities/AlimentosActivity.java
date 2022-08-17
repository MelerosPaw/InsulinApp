package julioverne.insulinapp.ui.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.services.ActualizarAlimentosService;
import julioverne.insulinapp.ui.adaptadores.AdaptadorAlimentos;
import julioverne.insulinapp.ui.adaptadores.ModoVisualizacion;
import julioverne.insulinapp.ui.callbacks.CardViewResizeAnimationManager;
import julioverne.insulinapp.ui.dialogs.DialogEditarAlimento;
import julioverne.insulinapp.ui.fragments.AlimentosFragment;
import julioverne.insulinapp.utils.IntentUtil;
import julioverne.insulinapp.utils.InternalMemoryUtils;
import julioverne.insulinapp.utils.StringUtils;
import julioverne.insulinapp.utils.ViewExtensions;
import julioverne.insulinapp.widgets.ScrollControlLayoutManager;

/**
 * Created by Juan José Melero on 09/05/2015.
 */
// TODO Melero 27/06/2022: Esta clase está siendo sustituida por FoodListFragment, no hay que arreglarla
public class AlimentosActivity extends BaseActivity {

    public static final String TAG = AlimentosActivity.class.getSimpleName();
    public static final String BUNDLE_FOR_RESULT = "BUNDLE_FOR_RESULT";
    public static final String BUNDLE_INCLUIDOS = "BUNDLE_INCLUIDOS";

    @BindView(R.id.et_nombre_alimento)
    EditText etCampoBusqueda;
    @BindView(R.id.fab_borrar_alimentos)
    FloatingActionButton btnBorrarAlimentos;
    @BindView(R.id.fab_crear_alimentos)
    FloatingActionButton btnCrearAlimentos;
    @BindView(R.id.fab_seleccionar_alimentos)
    FloatingActionButton btnSeleccionarAlimentos;
    @BindView(R.id.loading_view)
    FrameLayout loadingView;
    @BindView(R.id.rv_alimentos)
    RecyclerView rvAlimentos;

    private AdaptadorAlimentos adaptadorAlimentos;
    private Menu menu;
    private boolean isForResult;
    private List<AlimentoDAO> alimentosYaIncluidos;
    private CardViewResizeAnimationManager resizeManager;

    // Registra un receiver para recibir la respuesta del servicio de actualización de listaAlimentos. Si
    // llegan listaAlimentos, muestra un mensaje y recarga el adaptador, si no, solo muestra un mensaje.
    private final BroadcastReceiver receiverActualizacionAlimentos = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(ActualizarAlimentosService.RECARGAR, false)) {
                obtenerAlimentos(true);
            } else {
                StringUtils.toastCorto(context,
                    intent.getStringExtra(ActualizarAlimentosService.MENSAJE));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alimentos_layout);
        abrirParams();
        ButterKnife.bind(this);
        loadView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiverActualizacionAlimentos,
            new IntentFilter(Constants.ACTION_ACTUALIZACION_ALIMENTOS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverActualizacionAlimentos);
    }

    @Override
    public void onBackPressed() {
        // Si se ha realizado algún filtro, el botón atrás elimina el filtro
        //if (adaptadorAlimentos != null && adaptadorAlimentos.estaFiltrado()) {
        //    adaptadorAlimentos.filtrar("");
        //    etCampoBusqueda.setText("");
        //} else {
        //    dataManager.cancelarPeticionAlimentos();
        //    if (isForResult) {
        //        setResult(RESULT_CANCELED);
        //        finish();
        //    } else {
        //        super.onBackPressed();
        //    }
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_alimentos, menu);
        cambioOpcionVisualizacion(null, dataManager.getModoVisualizacion());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actualizar:
                obtenerAlimentos(true);
                return true;
            case R.id.modo_lista:
                cambioModoVisualizacion(item, ModoVisualizacion.LISTA);
                return true;
            case R.id.modo_tarjeta:
                cambioModoVisualizacion(item, ModoVisualizacion.TARJETAS);
                return true;
            case android.R.id.home:
                onHomeButtonPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void loadView() {
        //        cargarTipografiaCabecera();
        etCampoBusqueda.setTypeface(light);
        obtenerAlimentos(false);
    }

    private void abrirParams() {
        isForResult = getIntent().getBooleanExtra(BUNDLE_FOR_RESULT, false);
        alimentosYaIncluidos = getIntent().getParcelableArrayListExtra(BUNDLE_INCLUIDOS);
    }

    public void obtenerAlimentos(boolean actualizar) {
        //        showLoading(true);
        //        if (actualizar) {
        //            dataManager.getAlimentos(DataManager.DataSource.SERVER,
        //                    new DataManager.GetAlimentosCallback() {
        //                @Override
        //                public void onSuccess(List<AlimentoDAO> alimentos) {
        //                    adaptadorAlimentos.addAlimentos(alimentos);
        //                    if (!alimentos.isEmpty() && adaptadorAlimentos.estaFiltrado()) {
        //                        adaptadorAlimentos.filtrar(etCampoBusqueda.getText().toString());
        //                    }
        //                    showLoading(false);
        //                }
        //
        //                @Override
        //                public void onFailure() {
        //                    StringUtils.toastCorto(AlimentosActivity.this, "Error al intentar conectarse al servidor. Prueba otra vez más tarde.");
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
            new DataManager.GetAlimentosCallback() {
                @Override
                public void onSuccess(List<AlimentoDAO> alimentos) {
                    if (alimentos == null) {
                        StringUtils.toastLargo(AlimentosActivity.this, R.string.advertencia_recargar_alimentos);
                        AlimentosActivity.super.onBackPressed();
                    } else {
                        cargarAlimentos(alimentos);
                    }
                    showLoading(false);
                }

                @Override
                public void onFailure() {
                    StringUtils.toastLargo(AlimentosActivity.this, R.string.problema_al_obtener_alimentos);
                    AlimentosActivity.super.onBackPressed();
                    showLoading(false);
                }
            });
    }

    private void cambioModoVisualizacion(@Nullable MenuItem item,
        @NonNull ModoVisualizacion nuevoModo) {
        dataManager.cambioModoVisualizacionAlimentos(nuevoModo);
        adaptadorAlimentos.cambioModo(nuevoModo);
        cambioOpcionVisualizacion(item, nuevoModo);
    }

    private void cambioOpcionVisualizacion(@Nullable MenuItem item, @NonNull ModoVisualizacion nuevoModo) {
        ocultarOpcionActual(item, nuevoModo);
        mostrarOpcionNueva(nuevoModo);
    }

    private void ocultarOpcionActual(@Nullable MenuItem item, @NonNull ModoVisualizacion nuevoModo) {

        final MenuItem opcion;

        if (item != null) {
            opcion = item;
        } else {
            opcion = menu.findItem(nuevoModo == ModoVisualizacion.TARJETAS ?
                R.id.modo_tarjeta : R.id.modo_lista);
        }

        opcion.setVisible(false);
    }

    private void mostrarOpcionNueva(@NonNull ModoVisualizacion nuevoModo) {
        final int idOpcionVisible = nuevoModo == ModoVisualizacion.TARJETAS ?
            R.id.modo_lista : R.id.modo_tarjeta;
        menu.findItem(idOpcionVisible).setVisible(true);
    }

    private void cargarAlimentos(List<AlimentoDAO> alimentos) {
        final ScrollControlLayoutManager layoutManager = new ScrollControlLayoutManager(this);
        resizeManager = new CardViewResizeAnimationManager(rvAlimentos, layoutManager);
        adaptadorAlimentos = new AdaptadorAlimentos(this, alimentos, alimentosYaIncluidos, isForResult);
        adaptadorAlimentos.setOnEditarAlimento((alimento, posicion) -> mostrarDialogEditar(alimento,
            dataManager.getBitmapFromMemory(alimento.getImagen()), posicion));
        adaptadorAlimentos.setOnRedimensionarTarjeta(() -> resizeManager.resize());
        adaptadorAlimentos.setOnSeleccionarAlimento(this::activarModoSeleccion);

        rvAlimentos.setLayoutManager(layoutManager);
        rvAlimentos.setAdapter(adaptadorAlimentos);
    }

    private void activarModoSeleccion(boolean activado) {
        cambiarBotonesFlotantes(activado);
        cambiarActionBar(activado);
    }

    private void cambiarBotonesFlotantes(boolean activado) {
        ViewExtensions.visible(!activado, btnCrearAlimentos);
        ViewExtensions.visible(activado && !isForResult, btnBorrarAlimentos);
        ViewExtensions.visible(activado && isForResult, btnSeleccionarAlimentos);
    }

    private void cambiarActionBar(boolean activado) {

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            final @ColorRes int color = !activado ?
                R.color.primary : isForResult ?
                R.color.naranja_seleccion :
                R.color.rojo_borrar;
            final @StringRes int title = activado ?
                R.string.titulo_seleccion_alimentos :
                R.string.texto_titulo_seccion_alimentos;

            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, color));
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && dataManager.isRequestCodeForThisView(requestCode)) {
            showDialogDefinirAlimento(dataManager.getBitmapFromResult(this, requestCode, data));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onHomeButtonPressed() {
        onBackPressed();
    }

    public void actualizarLista(boolean correcto) {
        if (correcto) {
            obtenerAlimentos(false);
            etCampoBusqueda.setText("");
        }
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void mostrarDialogEliminacion(List<AlimentoDAO>[] alimentos) {
        AlertDialog.Builder dialogoEliminar = new AlertDialog.Builder(this);
        dialogoEliminar.setTitle(R.string.dialog_title);
        StringBuilder mensaje = new StringBuilder();
        String botonPositivo, botonNegativo;
        final List<AlimentoDAO> alimentosUsuario = alimentos[1];

        // Si solo se han escogido alimentos del sistema, solo se muestra un aviso
        if (alimentosUsuario.isEmpty()) {
            mensaje.append(getString(R.string.dialog_sin_seleccion));
            botonNegativo = getString(R.string.cancelar);
            botonPositivo = getString(R.string.dialog_entendido);

            // Si solo hay alimentos de usuario, se le indican los alimentos que se van a eliminar
        } else {
            mensaje.append(getString(R.string.dialog_message_se_eliminaran)).append("\n\n");
            for (AlimentoDAO a : alimentosUsuario) {
                mensaje.append("- ").append(a.getNombre()).append("\n");
            }
            mensaje.append("\n").append(getString(R.string.dialog_message_seguro));

            botonNegativo = getString(R.string.cancelar);
            botonPositivo = getString(R.string.dialog_borrar);
            //Si hay alimentos de los dos tipos, se le avisa de cuáles se van a eliminar
        }

        dialogoEliminar.setMessage(mensaje);
        dialogoEliminar.setPositiveButton(botonPositivo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eliminarAlimentos(alimentosUsuario);
            }
        });

        //Si solo se han escogido alimentos que no se pueden borrar, no muestra el botón de cancelar
        if (!alimentosUsuario.isEmpty()) {
            dialogoEliminar.setNegativeButton(botonNegativo, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        dialogoEliminar.show();
    }

    private void eliminarAlimentos(List<AlimentoDAO> alimentos) {
        //int size = alimentos.size();
        //int eliminados = dataManager.eliminarAlimentos(alimentos);
        //
        //// Si ha fallado la transacción, no se ha borrado nada
        //if (eliminados < size) {
        //    StringUtils.toastCorto(AlimentosActivity.this, R.string.advertencia_imposible_eliminar);
        //} else {
        //
        //    // Borra las imágenes de todos los alimentos
        //    int imagenesBorradas = 0;
        //
        //    for (final AlimentoDAO a : alimentos) {
        //        boolean borrada = dataManager.eliminarImagen(a.getImagen());
        //        imagenesBorradas = borrada ? imagenesBorradas + 1 : imagenesBorradas;
        //    }
        //
        //    Log.i(TAG, String.format(getString(R.string.advertencia_x_imagenes_borradas), imagenesBorradas));
        //    adaptadorAlimentos.eliminarBorrados(alimentos);
        //    StringUtils.toastCorto(AlimentosActivity.this, size == 1
        //        ? getString(R.string.dialog_eliminado_singular)
        //        : size + " " + getString(R.string.dialog_eliminado_plural));
        //}
    }

    private void crearAlimento(View view) {
        dataManager.seleccionarOrigenImagen(view, new DataManager.GetOrigenImagenCallback() {
            @Override
            public void onOrigenSelected(DataManager.OrigenImagen origen) {

                IntentUtil.IntentType intentType;

                if (origen == DataManager.OrigenImagen.DESDE_CAMARA) {
                    intentType = IntentUtil.IntentType.CAMERA;
                } else {
                    intentType = IntentUtil.IntentType.FILE_SELECT;
                }

                launchIntent(intentType);
            }
        });
    }

    private void borrarAlimentos() {
        mostrarDialogEliminacion(adaptadorAlimentos.obtenerMarcadosParaEliminar());
    }

    private void devolverAlimentos() {
        Intent intent = new Intent();
        intent.putExtra(AlimentosFragment.BUNDLE_CONTIENE, new ArrayList<>(adaptadorAlimentos.getSeleccionados()));
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Inicia un intent para obtener la imagen desde la camara o desde el sistema de archivos.
     */
    private void launchIntent(IntentUtil.IntentType type) {

        int requestCode = type == IntentUtil.IntentType.CAMERA ?
            Constants.CAMERA_INTENT_REQUEST_CODE : Constants.FILE_SEARCH_INTENT_REQUEST_CODE;

        Intent intent = IntentUtil.getIntent(this, type);
        if (intent != null) {
            switch (type) {
                case CAMERA:
                    StringUtils.toastLargo(this, R.string.advertencia_foto);
                    startActivityForResult(intent, requestCode);
                    break;
                case FILE_SELECT:
                    startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen..."), requestCode);
                    break;
            }
        } else {
            switch (type) {
                case CAMERA:
                    StringUtils.toastCorto(this, R.string.advertencia_sin_camara);
                    break;
                case FILE_SELECT:
                    StringUtils.toastCorto(this, R.string.advertencia_sin_galeria);
                    break;
            }
        }
    }

    private void definirAlimento(Bitmap bitmap) {

        // Redimensiona el Bitmap
        Bitmap b = InternalMemoryUtils.prepararBitmap(bitmap);
        mostrarDialogEditar(null, b, -1);
        loadingView.setVisibility(View.INVISIBLE);
    }

    private void mostrarDialogEditar(AlimentoDAO alimento, Bitmap bitmap, int posicion) {
        final DialogEditarAlimento fragment = DialogEditarAlimento.newInstance(alimento, posicion);
        //fragment.setUp(bitmap, new DialogEditarAlimento.OnFoodSavedListener() {
        //    @Override
        //    public void onSaved(AlimentoDAO alimento, int posicion) {
        //        adaptadorAlimentos.nuevoAlimento(alimento, posicion);
        //        if (posicion == -1) {
        //            StringUtils.toastCorto(AlimentosActivity.this, String.format(getString(R.string.alimento_anadido), alimento.getNombre()));
        //        }
        //    }
        //
        //    @Override
        //    public void onFailed(AlimentoDAO alimento) {
        //        Toast.makeText(AlimentosActivity.this, "No se ha podido guardar el alimento", Toast.LENGTH_SHORT).show();
        //    }
        //});

        fragment.show(AlimentosActivity.this.getSupportFragmentManager(), DialogEditarAlimento.TAG);
    }

    private void showDialogDefinirAlimento(final Bitmap bitmap) {
        if (bitmap != null) {
            // Después de llegar el Bitmap resultado, espera 1,5 segundos para que se reconstruya la
            // Activity si se ha girado el móvil para tomar la foto y luego llama al método
            // definirAlimento()
            showLoading(true);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    definirAlimento(bitmap);
                }
            }, 1500);
        } else {
            StringUtils.toastCorto(this, "Ha habido un problema al obtener la imagen.");
        }
    }

    @OnTextChanged(R.id.et_nombre_alimento)
    public void buscarAlimento() {
        //// Si no hay nada escrito, se muestran todos los listaAlimentos
        //// Si se ha escrito algo, se filtra la búsqueda por el nombre del alimento
        //adaptadorAlimentos.filtrar(etCampoBusqueda.getText().toString());

    }

    @OnClick({
        R.id.fab_borrar_alimentos,
        R.id.fab_crear_alimentos,
        R.id.fab_seleccionar_alimentos
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_borrar_alimentos:
                borrarAlimentos();
                break;
            case R.id.fab_crear_alimentos:
                crearAlimento(view);
                break;
            case R.id.fab_seleccionar_alimentos:
                devolverAlimentos();
                break;
        }
    }
}
