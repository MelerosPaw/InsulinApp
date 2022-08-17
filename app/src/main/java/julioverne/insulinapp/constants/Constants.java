package julioverne.insulinapp.constants;

/**
 * Created by Juan José Melero on 20/12/2015.
 */
public class Constants {

  public static final String APP_TAG = "INSULINAPP";
  public static final String APP_DIRECTORY = "InsulinApp";

  // Copia de seguridad
  public static final String BACKUP_DIRECTORY = "Copias Seguridad";
  public static final String DATABASE_BACKPUP_FILE_NAME = "copiaSeguridadBD.sqlite";
  public static final String SETTINGS_BACKPUP_FILE_NAME = "copiaSeguridadAjustes";
  public static final String IMAGES_DIRECTORY = "Imagenes";
  public static final long MS_ACTUALIZACION_INSULINA = 60000;

  // Intents
  public static final int CAMERA_INTENT_REQUEST_CODE = 0;
  public static final int FILE_SEARCH_INTENT_REQUEST_CODE = 1;
  public static final int SELECCION_ALIMENTOS_REQUEST_CODE = 2;

  public static final String ACTION_ACTUALIZACION_ALIMENTOS = "julioverne.insulinapp.ui.AlimentosActivity";

  // Opciones menú principal
  public static final String REALIZAR_CONTROL = "Realizar un control";
  public static final String DIARIO = "Diario";
  public static final String ALIMENTOS = "Alimentos";
  public static final String INSULINA_ACTUAL = "Insulina actual";
  public static final String ALARMAS = "Alarmas";

  // Opciones copia seguridad
  public static final String GUARDAR_COPIA = "Guardar";
  public static final String RESTAURAR_COPIA = "Restaurar";
  public static final String ELIMINAR_COPIA = "Eliminar";

  public static final int INFO_ALIMENTOS = 0;
  public static final int INFO_ENTRADAS = 1;
  public static final int INFO_PERIODOS = 2;
  public static final int INFO_AJUSTES = 3;
}
