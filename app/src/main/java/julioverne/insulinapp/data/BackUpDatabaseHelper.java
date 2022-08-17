package julioverne.insulinapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.data.dao.PeriodoDAO;
import melerospaw.memoryutil.MemoryUtil;
import melerospaw.memoryutil.Path;
import melerospaw.memoryutil.Result;

public class BackUpDatabaseHelper extends OrmLiteSqliteOpenHelper {

  public static final String DATABASE_NAME = Constants.DATABASE_BACKPUP_FILE_NAME;
  private static final int DATABASE_VERSION = 1;

  private static BackUpDatabaseHelper helper;

  private Dao<AlimentoDAO, String> alimentoDao;
  private Dao<EntradaDAO, Integer> entradaDao;
  private Dao<PeriodoDAO, Integer> periodoDao;

  private Context context;
  private Path databasePath;
  private Path databaseBackUpFile;

  public BackUpDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    databasePath = new Path.Builder(context)
        .databaseDirectory(DATABASE_NAME)
        .build();
    databaseBackUpFile = new Path.Builder(context)
        .storageDirectory(Path.STORAGE_PUBLIC_EXTERNAL)
        .folder(Constants.APP_DIRECTORY)
        .folder(Constants.BACKUP_DIRECTORY)
        .file(Constants.DATABASE_BACKPUP_FILE_NAME)
        .build();
    this.context = context.getApplicationContext();

    cargarBaseDatos();
  }

  // DATABASEHELPER SINGLETON METHODS
  public static BackUpDatabaseHelper getHelper(Context context) {
    //        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
    if (helper == null) {
      helper = new BackUpDatabaseHelper(context.getApplicationContext());
    }

    return helper;
  }

  public static void closeHelper() {
    if (helper != null) {
      helper.close();
      helper = null;
    }
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {

    try {
      TableUtils.createTable(connectionSource, AlimentoDAO.class);
      TableUtils.createTable(connectionSource, EntradaDAO.class);
      TableUtils.createTable(connectionSource, PeriodoDAO.class);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

  }

  private void cargarBaseDatos() {
    if (!MemoryUtil.exists(databasePath)) {
      copyDataBase();
    }
  }

  private void copyDataBase() {
    Result<File> crearCarpeta = MemoryUtil.createFolder("/data/data/" + context.getPackageName() + "/databases");
    if (!crearCarpeta.isSuccessful()) {
      Toast.makeText(context, crearCarpeta.getMessage(), Toast.LENGTH_LONG).show();
    } else {
      Result<File> importarBaseDatos = MemoryUtil.duplicateFile(databaseBackUpFile, databasePath);
      if (!importarBaseDatos.isSuccessful()) {
        Toast.makeText(context, importarBaseDatos.getMessage(), Toast.LENGTH_LONG).show();
      }
    }
  }

  private Dao<AlimentoDAO, String> getAlimentoDao() throws SQLException {
    if (alimentoDao == null) {
      alimentoDao = getDao(AlimentoDAO.class);
    }
    return alimentoDao;
  }

  private Dao<EntradaDAO, Integer> getEntradaDao() throws SQLException {
    if (entradaDao == null) {
      entradaDao = getDao(EntradaDAO.class);
    }
    return entradaDao;
  }

  private Dao<PeriodoDAO, Integer> getPeriodoDao() throws SQLException {
    if (periodoDao == null) {
      periodoDao = getDao(PeriodoDAO.class);
    }
    return periodoDao;
  }

  @Override
  public void close() {
    super.close();
    alimentoDao = null;
    entradaDao = null;
    periodoDao = null;
    //        OpenHelperManager.releaseHelper();
    eliminarBaseDatos();
  }

  private void eliminarBaseDatos() {
    if (!MemoryUtil.deleteFile(databasePath, false).isSuccessful()) {
      throw new IllegalStateException();
    }
  }

  public List<EntradaDAO> getEntradas() {
    try {
      return getEntradaDao().queryForAll();
    } catch (SQLException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  public List<AlimentoDAO> getAlimentos() {
    try {
      return getAlimentoDao().queryForAll();
    } catch (SQLException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  public List<PeriodoDAO> getPeriodos() {
    try {
      return getPeriodoDao().queryForAll();
    } catch (SQLException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }
}