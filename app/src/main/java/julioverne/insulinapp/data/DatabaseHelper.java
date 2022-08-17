package julioverne.insulinapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import julioverne.insulinapp.data.dao.AlimentoDAO;
import julioverne.insulinapp.data.dao.EntradaDAO;
import julioverne.insulinapp.data.dao.PeriodoDAO;
import melerospaw.memoryutil.MemoryUtil;
import melerospaw.memoryutil.Path;
import melerospaw.memoryutil.Result;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "InsulinApp.sqlite";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper helper;

    private Dao<AlimentoDAO, String> alimentoDao;
    private Dao<EntradaDAO, Integer> entradaDao;
    private Dao<PeriodoDAO, Integer> periodoDao;

    private Path databasePath;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        databasePath = new Path.Builder(context)
            .databaseDirectory(DATABASE_NAME)
            .build();
        cargarBaseDatos(context.getApplicationContext());
    }

    // DATABASEHELPER SINGLETON METHODS
    public static DatabaseHelper getHelper(Context context) {
        //        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
        if (helper == null) {
            helper = new DatabaseHelper(context.getApplicationContext());
        }

        return helper;
    }

    public static void closeHelper() {
        if (helper != null) {
            helper.close();
            helper = null;
        }
    }

    private void cargarBaseDatos(Context context) {
        if (sinBaseDeDatos()) {
            cargarDesdeAssets(context);
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
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion,
        int newVersion) {
        //Meter los alimentos nuevos.
    }

    /**
     * Checks if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean sinBaseDeDatos() {
        return !MemoryUtil.exists(databasePath);
    }

    private void cargarDesdeAssets(Context context) {
        copyDataBase(context);
    }

    /**
     * Copies your database from your local assets folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring bytestream.
     **/
    private void copyDataBase(Context context) {

        //        this.getReadableDatabase();

        String message;
        Result<File> crearCarpeta = MemoryUtil.createFolder("/data/data/" + context.getPackageName() + "/databases");
        if (!crearCarpeta.isSuccessful()) {
            message = crearCarpeta.getMessage();
        } else {
            Result<File> importarBaseDatos = MemoryUtil.importDatabaseFromAssets(context, DATABASE_NAME);
            message = importarBaseDatos.getMessage();
        }

        //        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    public Dao<AlimentoDAO, String> getAlimentoDao() {
        if (alimentoDao == null) {
            try {
                alimentoDao = getDao(AlimentoDAO.class);
            } catch (SQLException e) {
                throw new RuntimeException("No se ha podido crear el Dao de Alimento", e);
            }
        }
        return alimentoDao;
    }

    public Dao<EntradaDAO, Integer> getEntradaDao() throws SQLException {
        if (entradaDao == null) {
            entradaDao = getDao(EntradaDAO.class);
        }
        return entradaDao;
    }

    public Dao<PeriodoDAO, Integer> getPeriodoDao() throws SQLException {
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
    }

    public boolean isNombreAlimentoRepetido(String nombre) {
        QueryBuilder<AlimentoDAO, String> queryBuilder = getAlimentoDao().queryBuilder();
        queryBuilder.setCountOf(true);
        PreparedQuery<AlimentoDAO> preparedQuery;
        Where where = queryBuilder.where();

        try {
            where.eq(AlimentoDAO.FIELD_NOMBRE, nombre);
        } catch (SQLException e) {
            throw new RuntimeException("Error en el where al intentar comprobar si un nombre ya existe.", e);
        }

        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la sentencia preparada para " +
                "comprobar si un nombre ya existe.", e);
        }

        try {
            return getAlimentoDao().countOf(preparedQuery) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido comprobar si un nombre ya existe.", e);
        }
    }

    public boolean isNombreImagenRepetido(String nombreImagen) {
        QueryBuilder<AlimentoDAO, String> queryBuilder = getAlimentoDao().queryBuilder();
        queryBuilder.setCountOf(true);
        PreparedQuery<AlimentoDAO> preparedQuery;
        Where where = queryBuilder.where();

        try {
            where.eq(AlimentoDAO.FIELD_IMAGEN, nombreImagen);
        } catch (SQLException e) {
            throw new RuntimeException("Error en el where al intentar comprobar si un nombre ya existe.", e);
        }

        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la sentencia preparada para " +
                "comprobar si un nombre ya existe.", e);
        }

        try {
            return getAlimentoDao().countOf(preparedQuery) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido comprobar si un nombre ya existe.", e);
        }
    }

    public boolean crearAlimento(AlimentoDAO alimento) {
        try {
            return getAlimentoDao().create(alimento) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido guardar el alimento", e);
        }
    }

    public boolean eliminarAlimento(AlimentoDAO alimento) {
        try {
            return getAlimentoDao().delete(alimento) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido eliminar el alimento para actualizarlo", e);
        }
    }

    public List<PeriodoDAO> obtenerPeriodos() {
        try {
            return getPeriodoDao().queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener los periodos", e);
        }
    }
}
