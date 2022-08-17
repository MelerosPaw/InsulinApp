package julioverne.insulinapp.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.ArrayList;
import java.util.List;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.dao.ItemAEntrada;
import julioverne.insulinapp.databinding.ActivityControlLayoutBinding;
import julioverne.insulinapp.ui.base.NavigationFragment;
import julioverne.insulinapp.ui.fragments.AlimentosAConsumirFragment;
import julioverne.insulinapp.ui.fragments.DosisFragment;
import julioverne.insulinapp.ui.fragments.NivelGlucosaFragment;
import julioverne.insulinapp.ui.fragments.ResumenFragment;

public class ControlActivity extends NavigationFragment implements NivelGlucosaFragment.OnGlucoseInputListener {

    private ActivityControlLayoutBinding binding;
    private FragmentManager fragmentManager;
    private int glucosaSangre;
    private boolean postprandial;
    private List<ItemAEntrada> alimentosSeleccionados;

    //Gestiona el comportamiento del botón "Siguiente" según el Fragment que esté presente
    @NonNull
    public View.OnClickListener onNextClicked = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            boolean isCorrecto;
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
            String tag = currentFragment.getTag();

            if (tag.equals(NivelGlucosaFragment.TAG)) {
                final String glucosaSangreString =
                    ((NivelGlucosaFragment) currentFragment).getGlucosaSangre();
                onAmountOfGlucoseInBloodInput(glucosaSangreString);
            } else if (tag.equals(AlimentosAConsumirFragment.TAG)) {
                alimentosSeleccionados = ((AlimentosAConsumirFragment) currentFragment).getAlimentosSeleccionados();
                isCorrecto = comprobarAlimentosSeleccionados();
                if (isCorrecto) {
                    postprandial = ((AlimentosAConsumirFragment) currentFragment).getPostprandial();
                    fragmentManager.beginTransaction()
                        .addToBackStack(ResumenFragment.TAG)
                        .replace(R.id.fragmentContainer,
                            ResumenFragment.getInstance(glucosaSangre, alimentosSeleccionados, postprandial),
                            ResumenFragment.TAG)
                        .commit();
                }
            } else if (tag.equals(ResumenFragment.TAG)) {
                fragmentManager.beginTransaction()
                    .addToBackStack(DosisFragment.TAG)
                    .replace(R.id.fragmentContainer,
                        DosisFragment.getInstance(glucosaSangre, alimentosSeleccionados, postprandial),
                        DosisFragment.TAG)
                    .commit();
                binding.btnSiguiente.setText(R.string.guardar_entrada);
            } else if (tag.equals(DosisFragment.TAG)) {
                isCorrecto = ((DosisFragment) currentFragment).guardarEntrada();
                if (isCorrecto) {
                    volverAMainActivity();
                }
            }
        }
    };

    //Gestiona los casos del botón Atrás
    @NonNull
    public View.OnClickListener onBackClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            goBack();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        binding = ActivityControlLayoutBinding.inflate(inflater, container, false);
        fragmentManager = getChildFragmentManager();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Tipografías
        binding.setOnNextClicked(onNextClicked);
        binding.setOnBackClicked(onBackClicked);

        alimentosSeleccionados = new ArrayList<>();

        //Si se acaba de crear, mete el primer Fragment
        Fragment previousFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if (previousFragment == null) {
            fragmentManager.beginTransaction()
                .addToBackStack(NivelGlucosaFragment.TAG)
                .replace(R.id.fragmentContainer,
                    NivelGlucosaFragment.getInstance(glucosaSangre),
                    NivelGlucosaFragment.TAG)
                .commit();
        }

        binding.btnAtras.setText(R.string.salir);
    }

    @Override
    public void onResume() {
        super.onResume();
        //        mostrarInsulinaActual();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //region NivelGlucosaFragment.OnContinueListener
    @Override
    public void onContinue(@NonNull String glucoseAmount) {
        onAmountOfGlucoseInBloodInput(glucoseAmount);
    }
    //endregion

    @Override
    public void goBack() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        String tag = currentFragment.getTag();

        if (tag.equals(DosisFragment.TAG)) {
            // TODO Melero Vamos a hacer que esta activity sea un fragment con un gráfico de navegación propio
            // TODO Melero 29/06/2022: Se quiere volver al fragment anterior
            onBackPressed();
            binding.btnSiguiente.setText(R.string.siguiente);
        } else if (tag.equals(AlimentosAConsumirFragment.TAG)) {
            alimentosSeleccionados = ((AlimentosAConsumirFragment) currentFragment).getAlimentosSeleccionadosRaw();
            postprandial = ((AlimentosAConsumirFragment) currentFragment).getPostprandial();
            // TODO Melero 29/06/2022: Se quiere volver al fragment anterior
            super.onBackPressed();
            binding.btnAtras.setText(R.string.salir);
        } else if (tag.equals(NivelGlucosaFragment.TAG)) {
            //Si ha rellenado datos, pregunta con un Dialog si desea salir de verdad
            if (glucosaSangre != 0 || !alimentosSeleccionados.isEmpty()) {
                preguntarAlSalir(wantsToLeave -> {
                    if (wantsToLeave) {
                        volverAMainActivity();
                    }
                });
                //Si no, sale directamente
            } else {
                volverAMainActivity();
            }
        } else {
            super.goBack();
        }
    }

    private void onAmountOfGlucoseInBloodInput(@NonNull final String glucosaSangreString) {
        final Integer glucoseAmount = comprobarGlucosa(glucosaSangreString);

        if (glucoseAmount != null) {
            glucosaSangre = glucoseAmount;
            binding.btnAtras.setText(R.string.atras);
            fragmentManager.beginTransaction()
                .addToBackStack(AlimentosAConsumirFragment.TAG)
                .replace(R.id.fragmentContainer,
                    AlimentosAConsumirFragment.getInstance(postprandial, alimentosSeleccionados),
                    AlimentosAConsumirFragment.TAG)
                .commit();
        }
    }

    /**
     * Comprueba que la glucosa indicada no sea una cadena vacía. Si lo es, muestra un {@code Toast}.
     * Si no, asigna el nuevo valor a la variable de la clase {@code glucosaSangre}.
     *
     * @param glucosaSangreString Cantidad de glucosa en {@code String} que queremos comprobar.
     * @return Devuelve {@code true} si no es una cadena vacía y se puede convertir en {@code Integer}.
     */
    @Nullable
    private Integer comprobarGlucosa(@NonNull final String glucosaSangreString) {
        try {
            return Integer.parseInt(glucosaSangreString);
        } catch (NumberFormatException e) {
            toast(R.string.advertencias_sin_glucosa);
            return null;
        }
    }

    /**
     * Comprueba que la {@code List alimentosSelecccionados} no valga {@code null}.
     *
     * @return Devuelve {@code true} si no vale {@code null}.
     */
    private boolean comprobarAlimentosSeleccionados() {
        return alimentosSeleccionados != null;
    }

    /**
     * Vuelve a la MainActivity borando la BackStack
     */
    private void volverAMainActivity() {
        super.goBack();
        //Intent intent = new Intent(this, NavigationActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intent);
    }
}
