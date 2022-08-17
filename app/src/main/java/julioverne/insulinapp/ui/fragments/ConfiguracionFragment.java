package julioverne.insulinapp.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import julioverne.insulinapp.R;
import julioverne.insulinapp.data.SharedPreferencesManager;
import julioverne.insulinapp.sharedpreferences.ExtendedEditTextPreference;
import julioverne.insulinapp.sharedpreferences.TimePreference;

/**
 * Created by Juan José Melero on 26/04/2015.
 */
public class ConfiguracionFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registrar escucha
        getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Cancelar registro de la escucha
        getPreferenceScreen().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }

    //Cambia el valor actual que se muestra en el ítem de la preferencia afectada
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        actualizarValorActual(key);
    }

    // Actualiza el valor que se muestra en cada preferencia
    public void actualizarValorActual(String key) {

        Preference preference;

        switch (key) {
            case SharedPreferencesManager.DURACION_HORAS:
            case SharedPreferencesManager.DURACION_MINUTOS:
                preference = (findPreference(SharedPreferencesManager.DURACION_INSULINA));
                break;
            case SharedPreferencesManager.BACKUP_HORAS:
            case SharedPreferencesManager.BACKUP_MINUTOS:
                preference = (findPreference(SharedPreferencesManager.BACKUP_TIEMPO));
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
}
