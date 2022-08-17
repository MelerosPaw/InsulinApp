package julioverne.insulinapp.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.data.dao.PeriodoDAO;
import julioverne.insulinapp.ui.ResultadoCopiaSeguridad;
import julioverne.insulinapp.utils.CustomTypefaceSpan;
import julioverne.insulinapp.utils.DecimalFormatUtils;
import julioverne.insulinapp.utils.StringUtils;
import julioverne.insulinapp.utils.TimeUtils;
import julioverne.insulinapp.utils.TypefacesUtils;
import melerospaw.memoryutil.MemoryUtil;
import melerospaw.memoryutil.Path;
import melerospaw.memoryutil.Result;

/**
 * Created by Juan José Melero on 24/12/2016.
 */

public class BackUpManager {

    private static final int DB = 1;
    private static final int ALL = 2;
    private static final int SETTINGS = 3;
    private static final int IMAGES = 4;
    private static final int NONE = 5;

    private Path databasePath;
    private Path databaseBackUpFile;
    private Path.Builder imagesDirectoryPath;
    private Path.Builder imagesBackUpDirectoryPath;
    private Path settingsBackUpPath;
    private Context context;

    BackUpManager(Context context, Path databasePath, Path.Builder imagesDirectory) {
        this.context = context;
        this.databasePath = databasePath;
        this.imagesDirectoryPath = imagesDirectory;
        this.databaseBackUpFile = new Path.Builder(context)
            .storageDirectory(Path.STORAGE_PUBLIC_EXTERNAL)
            .folder(Constants.APP_DIRECTORY)
            .folder(Constants.BACKUP_DIRECTORY)
            .file(Constants.DATABASE_BACKPUP_FILE_NAME)
            .build();
        this.imagesBackUpDirectoryPath = new Path.Builder(context)
            .storageDirectory(Path.STORAGE_PUBLIC_EXTERNAL)
            .folder(Constants.APP_DIRECTORY)
            .folder(Constants.IMAGES_DIRECTORY);
        this.settingsBackUpPath = new Path.Builder(context)
            .storageDirectory(Path.STORAGE_PUBLIC_EXTERNAL)
            .folder(Constants.APP_DIRECTORY)
            .folder(Constants.BACKUP_DIRECTORY)
            .file(Constants.SETTINGS_BACKPUP_FILE_NAME)
            .build();
    }

    private BackUpDatabaseHelper getHelper() {
        return BackUpDatabaseHelper.getHelper(context);
    }

    boolean hayCopiaSeguridad() {
        return hayCopiaImagenes() && hayCopiaAjustes() && hayCopiaBD();
    }

    private boolean hayCopiaBD() {
        return MemoryUtil.exists(databaseBackUpFile);
    }

    private boolean hayCopiaImagenes() {
        return MemoryUtil.exists(imagesBackUpDirectoryPath.build()) &&
            !MemoryUtil.isFolderEmpty(imagesBackUpDirectoryPath.build().getFile(), false);
    }

    private boolean hayCopiaAjustes() {
        return MemoryUtil.exists(settingsBackUpPath);
    }

    /**
     * Realiza una copia de seguridad completa, lo cual incluye la base de datos, las imágenes de
     * los alimentos y las preferencias.
     *
     * @param sharedPreferences El archivo de preferencias que se quiere almacenar.
     * @return Un archivo {@code ResultadoCopiaSeguridad} que contendrá true si la copia se ha
     * realizado correctamente.
     */
    ResultadoCopiaSeguridad realizarCopiaSeguridad(SharedPreferences sharedPreferences) {

        String message;
        boolean copiaRealizada;

        if (!MemoryUtil.isExternalMemoryAvailable()) {
            message = context.getString(R.string.sin_tarjeta_sd);
            copiaRealizada = false;
        } else {
            BackUpDatabaseHelper.closeHelper();
            boolean copiaBaseDatos = realizarCopiaBD();
            boolean copiaImagenes = realizarCopiaImagenes();
            boolean copiaPreferencias = realizarCopiaAjustes(sharedPreferences);

            copiaRealizada = copiaBaseDatos && copiaPreferencias && copiaImagenes;
            message = getMensajeCopiaRealizada(copiaRealizada);
        }

        return new ResultadoCopiaSeguridad(copiaRealizada, message);
    }

    @NonNull
    private String getMensajeCopiaRealizada(boolean copiaCompletada) {
        return copiaCompletada ? "Copia realizada correctamente." : "No se ha podido completar la copia.";
    }

    /**
     * Guarda una copia de la base de datos en la tarjeta SD para restaurarla si se desinstala la
     * aplicación.
     *
     * @return Devuelve true si la copia se ha realizado correctamente, y false en caso contrario.
     */
    private boolean realizarCopiaBD() {
        return MemoryUtil.duplicateFile(databasePath, databaseBackUpFile).isSuccessful();
    }

    /**
     * Guarda una copia de las SharedPreferences en la tarjeta SD para restaurarla si se desinstala
     * la aplicación.
     *
     * @return Devuelve true si la copia se ha realizado correctamente, y false en caso contrario.
     */
    private boolean realizarCopiaAjustes(SharedPreferences sharedPreferences) {
        return MemoryUtil.saveSharedPreferences(sharedPreferences, settingsBackUpPath).isSuccessful();
    }

    /**
     * Guarda una copia de las imágenes en la tarjeta SD para restaurarlo si se desinstala la
     * aplicación.
     *
     * @return Devuelve true si la copia se ha realizado correctamente, y false en caso contrario.
     */
    private boolean realizarCopiaImagenes() {
        return MemoryUtil.duplicateFolder(imagesDirectoryPath.build(), imagesBackUpDirectoryPath.build())
            .isSuccessful();
    }











    /*RESTAURAR*/

    public void restaurarCopiaSeguridad(AlertDialog.Builder builder, SharedPreferences sharedPreferences,
        final RestoreListener restoreListener) {

        Object[] message_type = getDialogMessageAndRestoreType();
        String dialogMessage = (String) message_type[0];
        //        List<Integer> restoreTypes = (List<Integer>) message_type[1];

        builder.setTitle("Restaurar copia de seguridad").setMessage(dialogMessage);
        setPositiveListener(builder, restoreListener, sharedPreferences);
        setNegativeListener(builder, restoreListener);
        builder.show();
    }

    /**
     * Comprueba si hay copia de seguridad de las SharedPreferences o de la base de datos y devuelve
     * el un array con [0] el mensaje que se debe moistrar en el dialog y [1] el tipo de
     * recuperación que se va a realizar.
     */
    private Object[] getDialogMessageAndRestoreType() {

        String dialogMessage;
        List<Integer> restoreTypes = new Stack<>();

        if (!hayCopiaSeguridad()) {
            restoreTypes.add(NONE);
        } else if (hayCopiaSeguridad()) {
            restoreTypes.add(ALL);
        } else {
            if (hayCopiaBD()) {
                restoreTypes.add(DB);
            }
            if (hayCopiaAjustes()) {
                restoreTypes.add(SETTINGS);
            }
            if (hayCopiaImagenes()) {
                restoreTypes.add(NONE);
            }
        }

        dialogMessage = getRestoreDialogMessage(restoreTypes);

        return new Object[] {
            dialogMessage,
            restoreTypes
        };
    }

    public String getRestoreDialogMessage(List<Integer> restoreTypes) {

        StringBuilder dialogMessage = new StringBuilder();
        dialogMessage.append("Se ha encontrado una copia de seguridad de:\n");

        for (Integer restoreType : restoreTypes) {
            switch (restoreType) {
                case NONE:
                    dialogMessage.delete(0, dialogMessage.length() - 1);
                    dialogMessage.append("No hay ninguna copia de seguridad guardada.");
                    break;
                case ALL:
                    dialogMessage.delete(0, dialogMessage.length() - 1);
                    dialogMessage.append("Se ha encontrado una copia de seguridad de sus " +
                        "entradas, alimentos y ajustes ¿Quieres restaurarla?");
                    break;
                case DB:
                    dialogMessage.append("\t- Entradas y alimentos");
                    break;
                case IMAGES:
                    dialogMessage.append("\t- Imágenes de alimentos");
                    break;
                case SETTINGS:
                    dialogMessage.append("\t- Ajustes");
                    break;
                default:
            }

            if (restoreType.equals(NONE) || restoreType.equals(ALL)) {
                break;
            } else {
                dialogMessage.append("\n");
            }
        }

        if (!restoreTypes.contains(NONE) && !restoreTypes.contains(ALL)) {
            dialogMessage.append("¿Quieres restaurarla?");
        }

        return dialogMessage.toString();
    }

    private AlertDialog.Builder setPositiveListener(AlertDialog.Builder builder,
        final RestoreListener restoreListener,
        final SharedPreferences sharedPreferences) {

        if (hayCopiaSeguridad()) {
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String resultMessage;

                    boolean restored = restoreBackUp(sharedPreferences);

                    if (restored) {
                        resultMessage = "Se ha resturado la copia de seguridad.";
                    } else {
                        resultMessage = "No se ha podido restaurar la copia de seguridad.";
                    }

                    restoreListener.isRestoring(new ResultadoCopiaSeguridad(restored, resultMessage));
                    dialog.dismiss();
                }
            });
        }

        return builder;
    }

    private AlertDialog.Builder setNegativeListener(AlertDialog.Builder builder,
        final RestoreListener restoreListener) {

        String negativeButton = hayCopiaSeguridad() ? "No" : "De acuerdo";
        builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                restoreListener.isNotRestoring();
            }
        });

        return builder;
    }

    /**
     * Restaura la copia de seguridad. Restaura la base de datos, las preferencias y las imágenes de
     * los alimentos.
     *
     * @return True si se restuara.
     */
    public boolean restoreBackUp(SharedPreferences sharedPreferences) {
        boolean dbRestored = restaurarBaseDatos();
        boolean settingsRestored = restaurarAjustes(sharedPreferences);
        boolean imagesRestored = restaurarImagenes();

        return dbRestored && settingsRestored && imagesRestored;
    }

    /**
     * Restaura la base de datos.
     *
     * @return True si se restaura.
     */
    private boolean restaurarBaseDatos() {

        boolean restaurada;

        Result eliminado = MemoryUtil.deleteFile(databasePath, false);

        if (eliminado.isSuccessful()) {
            // MemoryUtil no crea el directorio data/data/package.name/databases.
            // Hay que crearlo a mano.
            File databaseDirectory = new File("/data/data/" + context.getPackageName() + "/databases");
            databaseDirectory.mkdir();

            if (!databaseDirectory.exists()) {
                Log.e(Constants.APP_TAG, "No se ha podido crear el directorio de la base de datos.");
                StringUtils.toastLargo(context, "No se ha podido crear el directorio de la base de datos.");
                restaurada = Boolean.FALSE;
            } else {
                Result<File> resultado = MemoryUtil.duplicateFile(databaseBackUpFile, databasePath);
                if (resultado.isSuccessful()) {
                    DatabaseHelper.closeHelper();
                }
                restaurada = resultado.isSuccessful();
            }
        } else {
            restaurada = eliminado.isSuccessful();
        }

        return restaurada;
    }

    /**
     * Restaura las SharedPreferences.
     *
     * @return True si se resturan.
     */
    private boolean restaurarAjustes(SharedPreferences sharedPreferences) {
        return MemoryUtil.loadSharedPreferences(settingsBackUpPath, sharedPreferences).isSuccessful();
    }

    /**
     * Restaura la carpeta de imágenes.
     *
     * @return True si se copian todas las imágenes.
     */
    private boolean restaurarImagenes() {
        return MemoryUtil.duplicateFolder(imagesBackUpDirectoryPath.build(), imagesDirectoryPath.build())
            .isSuccessful();
    }

    /*ELIMINAR*/

    public Result eliminarCopia() {
        getHelper().close();
        return MemoryUtil.deleteFile(databaseBackUpFile, false);
    }

    /*BASE DATOS COOPIA SEGURIDAD*/

    public SpannableString getDescripcionCopiaSeguridad() {

        File copiaBaseDatos = this.databaseBackUpFile.getFile();
        File copiaImagenes = this.imagesBackUpDirectoryPath.build().getFile();
        File copiaAjustes = this.settingsBackUpPath.getFile();

        Long pesoBaseDatos = copiaBaseDatos.getTotalSpace();
        Long pesoImagenes = copiaImagenes.getTotalSpace();
        Long pesoAjustes = copiaAjustes.getTotalSpace();

        String peso = StringUtils.humanReadableByteCount(pesoBaseDatos + pesoImagenes + pesoAjustes, false)
            .replace("i", "")
            .replace(".", ",");

        Date fechaCreacion = new Date(copiaBaseDatos.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM',' yyyy',' kk:mm");

        String descripcion = "Realizada el " + sdf.format(fechaCreacion) + "\n"
            + "Tamaño: " + peso;

        SpannableString message = new SpannableString(descripcion);
        message.setSpan(new CustomTypefaceSpan(
                TypefacesUtils.get(context, "fonts/DejaVuSansCondensed-Oblique.ttf")),
            descripcion.indexOf('T'), descripcion.indexOf(':', descripcion.indexOf('T') + 1), 0);

        return message;
    }

    public String getResumenCopiaSeguridad(int tipo) {

        String informacion;

        switch (tipo) {
            case Constants.INFO_ALIMENTOS:
                informacion = getResumenAlimentos();
                break;
            case Constants.INFO_ENTRADAS:
                informacion = getResumenEntradas();
                break;
            case Constants.INFO_PERIODOS:
                informacion = getResumenPeriodos();
                break;
            default:
                informacion = getResumenAjustes();
                break;
        }

        return informacion.trim();
    }

    @NonNull
    private String getResumenAlimentos() {

        StringBuilder listaAlimentos = new StringBuilder();
        List<AlimentoDAO> alimentos = getHelper().getAlimentos();

        if (alimentos.isEmpty()) {
            listaAlimentos.append("Sin alimentos en la copia de seguridad.");
        } else {
            listaAlimentos.append("Alimentos en la copia de seguridad\n");
            for (AlimentoDAO alimento : alimentos) {
                listaAlimentos.append(" - ").append(alimento.getNombre()).append("\n");
            }
        }

        return listaAlimentos.toString();
    }

    @NonNull
    private String getResumenPeriodos() {

        StringBuilder listaPeriodos = new StringBuilder();
        List<PeriodoDAO> periodos = getHelper().getPeriodos();

        if (periodos.isEmpty()) {
            listaPeriodos.append("Sin periodos en la copia de seguridad.");
        } else {
            for (PeriodoDAO periodo : periodos) {
                int horaInicio = periodo.getHoraInicio();
                int minutosInicio = periodo.getMinutosInicio();
                int horaFin = periodo.getHoraFin();
                int minutosFin = periodo.getMinutosFin();

                listaPeriodos.append(String.format("%1$s - %2$s: %3$s uds.%n",
                    TimeUtils.getHoraFormateada(horaInicio, minutosInicio),
                    TimeUtils.getHoraFormateada(horaFin, minutosFin),
                    DecimalFormatUtils.decimalToStringIfZero(periodo.getUnidadesInsulina(), 0, ".", ",")));
            }
        }

        return listaPeriodos.toString();
    }

    @NonNull
    private String getResumenEntradas() {
        StringBuilder listaEntradas = new StringBuilder();
        List<EntradaDAO> entradas = getHelper().getEntradas();
        if (entradas.isEmpty()) {
            listaEntradas.append("Sin entradas de diario en la copia de seguridad.");
        } else {
            listaEntradas.append(String.format("Hay un total de %1$d entradas almacenadas.%n", entradas.size()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy, kk:mm");
            for (EntradaDAO entrada : entradas) {
                listaEntradas.append(String.format(" - %1$s: \t%2$smg/ml%n",
                    sdf.format(entrada.getFecha()), entrada.getGlucosaSangre()));
            }
        }

        return listaEntradas.toString();
    }

    @NonNull
    private String getResumenAjustes() {
        return new SharedPreferencesManager(context).createPreferenceSummary(settingsBackUpPath);
    }

    public interface RestoreListener {
        void isRestoring(ResultadoCopiaSeguridad resultado);

        void isNotRestoring();
    }
}
