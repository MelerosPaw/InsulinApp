package julioverne.insulinapp.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import julioverne.insulinapp.R;
import julioverne.insulinapp.ui.adaptadores.ModoVisualizacion;
import julioverne.insulinapp.utils.StringUtils;
import julioverne.insulinapp.utils.TimeUtils;
import melerospaw.memoryutil.MemoryUtil;
import melerospaw.memoryutil.Path;
import melerospaw.memoryutil.Result;

import static julioverne.insulinapp.utils.StringUtils.getEmptyIfNull;

/**
 * Created by Juan José Melero on 01/08/2016.
 */
public class SharedPreferencesManager {

    public static final String MENSAJE_RESTAURAR_MOSTRADO = "mensaje_restaurar_mostrado";
    public static final String DURACION_INSULINA = "duracion_insulina";
    public static final String DURACION_HORAS = "duracion_horas";
    public static final String DURACION_MINUTOS = "duracion_minutos";
    public static final String FACTOR_SENSIBILIDAD = "factor_sensibilidad";
    public static final String GLUCOSA_MINIMA = "glucosa_minima";
    public static final String GLUCOSA_MAXIMA = "glucosa_maxima";
    public static final String IMAGENES_CARGADAS = "imagenes_cargadas";
    public static final String BACKUP_ENABLED = "backup_enabled";
    public static final String BACKUP_TIEMPO = "backup_tiempo";
    public static final String BACKUP_HORAS = "hora_backup";
    public static final String BACKUP_MINUTOS = "minutos_backup";
    public static final String MODO_VISUALIZACION = "modo_visualizacion";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SharedPreferencesManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setDefaultPreferences() {
        openEditor();
        editor.putString(DURACION_HORAS, "2");
        editor.putString(DURACION_MINUTOS, "30");
        editor.putString(FACTOR_SENSIBILIDAD, "30");
        editor.putString(GLUCOSA_MINIMA, "90");
        editor.putString(GLUCOSA_MAXIMA, "300");
        //        editor.putString(BACKUP_HORAS, "5");
        //        editor.putString(BACKUP_MINUTOS, "00");
        closeEditor();
    }

    public void setImagenesCargadas(boolean value) {
        openEditor();
        editor.putBoolean(IMAGENES_CARGADAS, value);
        closeEditor();
    }

    public String get(String preferenceName) {
        return sharedPreferences.getString(preferenceName, "");
    }

    public boolean getBoolean(String preferenceName) {
        return sharedPreferences.getBoolean(preferenceName, false);
    }

    public void set(String preferenceName, Object value) {

        openEditor();

        switch (preferenceName) {
            case MENSAJE_RESTAURAR_MOSTRADO:
            case IMAGENES_CARGADAS:
            case BACKUP_ENABLED:
                editor.putBoolean(preferenceName, (boolean) value);
                break;
            default:
                editor.putString(preferenceName, (String) value);
        }

        closeEditor();
    }

    /**
     * Devuelve un {@code String[]} con los valores de las preferencias indicadas. Si falta alguna,
     * devuelve null y ejecuta un Toast indicando qué preferencia falta por configurar.
     *
     * @param preferenciasSolicitadas Claves de las preferencias solicitadas.
     * @return Un {@code String[]} con los valores de las preferencias solicitadas en el mismo orden
     * que {@code preferenciasSolicitadas}.
     */
    public String[] getPreferencias(String[] preferenciasSolicitadas) {

        String[] preferencias = new String[preferenciasSolicitadas.length];
        String preferencia;
        List<String> falta = new ArrayList<>();

        for (int i = 0; i < preferenciasSolicitadas.length; i++) {
            preferencia = this.get(preferenciasSolicitadas[i]);
            if (!preferencia.isEmpty()) {
                preferencias[i] = preferencia;
            } else {
                falta.add(preferenciasSolicitadas[i]);
                break;
            }
        }

        if (falta.isEmpty()) {
            return preferencias;
        } else {
            StringBuilder ausentes = new StringBuilder();

            for (String s : falta) {
                ausentes.append(s);
                if (falta.indexOf(s) != falta.size() - 1) {
                    ausentes.append(",");
                }
            }

            StringUtils.toastLargo(context, context.getResources()
                .getQuantityString(R.plurals.advertencia_faltan_permisos, falta.size(), ausentes.toString()));
            return new String[0];
        }
    }

    /**
     * Devuelve true si todas las preferencias necesarias para realizar un control han sido
     * establecidas.
     */
    public boolean arePreferencesSet() {
        boolean isMinimaSet = !get(GLUCOSA_MINIMA).isEmpty();
        boolean isMaximaSet = !get(GLUCOSA_MAXIMA).isEmpty();
        boolean isDuracionHorasSet = !get(DURACION_HORAS).isEmpty();
        boolean isDuracionMinutosSet = !get(DURACION_MINUTOS).isEmpty();
        boolean isFactorSensibilidadSet = !get(FACTOR_SENSIBILIDAD).isEmpty();
        return isMinimaSet && isMaximaSet && isDuracionHorasSet && isDuracionMinutosSet &&
            isFactorSensibilidadSet;
    }

    public String createPreferenceSummary(Path settingsBackUpPath) {
        Result<Map<String, Object>> result = MemoryUtil.loadSharedPreferences(settingsBackUpPath);
        if (result.isSuccessful()) {
            Map<String, ?> mappedPreferences = result.getResult();
            StringBuilder summary = new StringBuilder();

            String duracionHoras = getEmptyIfNull((String) mappedPreferences.get(DURACION_HORAS));
            String duracionMinutos = getEmptyIfNull((String) mappedPreferences.get(DURACION_MINUTOS));
            String factorSensibilidad = getEmptyIfNull((String) mappedPreferences.get(FACTOR_SENSIBILIDAD));
            String glucosaMax = getEmptyIfNull((String) mappedPreferences.get(GLUCOSA_MAXIMA));
            String glucosaMin = getEmptyIfNull((String) mappedPreferences.get(GLUCOSA_MINIMA));

            Boolean backUp = (Boolean) mappedPreferences.get(BACKUP_ENABLED);
            String copiaSeguridadAutomatica = "Desactivada";
            String copiaSeguridadHoras = "", copiaSeguridadMinutos = "";
            if (backUp != null && backUp) {
                copiaSeguridadAutomatica = "Activada";
                copiaSeguridadHoras = getEmptyIfNull((String) mappedPreferences.get(BACKUP_HORAS));
                copiaSeguridadMinutos = getEmptyIfNull((String) mappedPreferences.get(BACKUP_MINUTOS));
            }

            if (!TextUtils.isEmpty(duracionHoras) && !TextUtils.isEmpty(duracionMinutos)) {
                summary.append(String.format("Duración de la glucosa: %1$s",
                    TimeUtils.getHoraFormateada(duracionHoras, duracionMinutos)))
                    .append("\n");
            }

            if (!TextUtils.isEmpty(factorSensibilidad)) {
                summary.append("Factor de sensibilidad: ")
                    .append(factorSensibilidad)
                    .append("\n");
            }

            if (!TextUtils.isEmpty(glucosaMax)) {
                summary.append("Glucosa máxima: ")
                    .append(glucosaMax)
                    .append("\n");
            }

            if (!TextUtils.isEmpty(glucosaMin)) {
                summary.append("Glucosa mínima: ")
                    .append(glucosaMin)
                    .append("\n");
            }

            summary.append("Copia de seguridad: ")
                .append(copiaSeguridadAutomatica)
                .append("\n");

            if (copiaSeguridadAutomatica.equals("Activada")) {
                summary.append(String.format("Hora copia de seguridad: %1$s%n",
                    TimeUtils.getHoraFormateada(copiaSeguridadHoras, copiaSeguridadMinutos)));
            }

            return summary.toString();
        } else {
            return null;
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void openEditor() {
        editor = this.sharedPreferences.edit();
    }

    private void closeEditor() {
        editor.apply();
    }

    public boolean isCopiaSeguridadActiva() {
        return getBoolean(SharedPreferencesManager.BACKUP_ENABLED);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public String getHoraCopiaSeguridad() {
        String horas = get(BACKUP_HORAS);
        String minutos = TimeUtils.ceroCero(get(BACKUP_MINUTOS));
        return horas + ":" + minutos;
    }

    public boolean isHoraCopiaSeguridadSet() {
        return !":".equals(getHoraCopiaSeguridad());
    }

    public void cambioModoVisualizacion(@NonNull ModoVisualizacion nuevoModo) {
        openEditor();
        editor.putInt(MODO_VISUALIZACION, nuevoModo.ordinal());
        closeEditor();
    }

    public ModoVisualizacion getModoVisualizacion() {
        final int modoAlmacenado = sharedPreferences.getInt(MODO_VISUALIZACION,
            ModoVisualizacion.TARJETAS.ordinal());
        return ModoVisualizacion.values()[modoAlmacenado];
    }
}
