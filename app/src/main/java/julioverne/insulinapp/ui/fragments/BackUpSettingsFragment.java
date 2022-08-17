package julioverne.insulinapp.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.DataManager;
import julioverne.insulinapp.data.SharedPreferencesManager;
import julioverne.insulinapp.sharedpreferences.ExtendedEditTextPreference;
import julioverne.insulinapp.sharedpreferences.TimePreference;

public class BackUpSettingsFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private DataManager dataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = DataManager.getInstance(getActivity());
        addPreferencesFromResource(R.xml.preferencias_copia_de_seguridad);
        habilitarTiempoCopia(SharedPreferencesManager.BACKUP_ENABLED);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registrar escucha
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Cancelar registro de la escucha
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    // Cambia el valor actual que se muestra en el ítem de la preferencia afectada
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        actualizarValorActual(key);
        habilitarTiempoCopia(key);
    }

    // Actualiza el valor que se muestra en cada preferencia
    public void actualizarValorActual(String key) {

        Preference preference;

        switch (key) {
            case SharedPreferencesManager.DURACION_HORAS:
            case SharedPreferencesManager.DURACION_MINUTOS:
                preference = findPreference(SharedPreferencesManager.DURACION_INSULINA);
                break;
            case SharedPreferencesManager.BACKUP_HORAS:
            case SharedPreferencesManager.BACKUP_MINUTOS:
                preference = findPreference(SharedPreferencesManager.BACKUP_TIEMPO);
                break;
            default:
                preference = findPreference(key);
        }

        if (preference instanceof ExtendedEditTextPreference) {
            ((ExtendedEditTextPreference) preference).loadView();
        } else if (preference instanceof TimePreference) {
            ((TimePreference) preference).loadView();
        }
    }

    // In/habilita el campo de la hora de la copia de seguridad según se des/active la copia
    public void habilitarTiempoCopia(String key) {
        if (key.equals(SharedPreferencesManager.BACKUP_ENABLED)) {
            TimePreference preferenciaTiempoCopia =
                (TimePreference) findPreference(SharedPreferencesManager.BACKUP_TIEMPO);
            preferenciaTiempoCopia.setEnabled(dataManager.isCopiaSeguridadActiva());

            if (dataManager.isCopiaSeguridadActiva()) {
                if (!dataManager.isHoraCopiaSeguridadEstablecida()) {
                    preferenciaTiempoCopia.setDefaultValue(null);
                } else {
                    preferenciaTiempoCopia.actualizarValorMostrado();
                }
            }
        }
    }
}
