package julioverne.insulinapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import julioverne.insulinapp.data.DataManager;
import melerospaw.memoryutil.MemoryUtil;
import melerospaw.memoryutil.Path;
import melerospaw.memoryutil.Result;

/**
 * Created by Juan José Melero on 26/05/2015.
 */
public class InternalMemoryUtils {

    private static final String TAG = InternalMemoryUtils.class.getSimpleName();

    private InternalMemoryUtils() {
    }

    /**
     * Devuelve un {@code Bitmap} a partir de una imagen almacenada en
     * <i>data/data/nuestraApp/files</i>.
     *
     * @param nombreImagen Con MemoryUtils es el nombre de la imagen. Antes era donde se encuentra
     * la imagen a partir de <i>data/data/nuestraApp/files</i>.
     * @return Devuelve el {@code Bitmap} de la imagen indicada o <i>null</i> si no existe la imagen.
     */
    @Nullable
    public static Bitmap obtenerImagenBitmap(
        @NonNull final String nombreImagen,
        @NonNull final Path.Builder path
    ) {

        final Bitmap bitmap;
        final Path rutaImagen = path.file(nombreImagen).build();

        if (MemoryUtil.exists(rutaImagen)) {
            Result<Bitmap> resultadoBitmap = MemoryUtil.loadBitmap(rutaImagen);
            if (resultadoBitmap.isSuccessful()) {
                bitmap = resultadoBitmap.getResult();
            } else {
                bitmap = null;
            }
        } else {
            Log.e(TAG, "No se ha podido obtener la imagen en " + rutaImagen);
            bitmap = null;
        }

        return bitmap;
    }

    /**
     * Guarda una imagen en la memoria interna del dispositivo, en <i>data/data/app/files</i>,
     * comprobando antes que no exista ya una igual.
     *
     * @param bitmap Objeto <i>Bitmap</i> de la imagen. Sus medidas deben ser de 142px x 142px
     * @param nombreImagen Nombre que queremos que tenga la imagen en
     * <i>data/data/nuestraApp/files</i>. No debe contener espacios.
     * @return Devuelve un booleano que indica si la imagen se ha guardado correctamente o no.
     */
    public static boolean guardarImagen(final Bitmap bitmap, final String nombreImagen,
        Path.Builder ruta) {

        boolean imagenGuardada;

        if (bitmap != null) {
            Path rutaImagen = ruta.file(nombreImagen).build();
            Result<File> resultadoGuardada = MemoryUtil.saveBitmap(bitmap, rutaImagen);
            imagenGuardada = resultadoGuardada.isSuccessful();

            if (!resultadoGuardada.isSuccessful()) {
                Log.e(TAG, "No se ha guardado la imagen. Puede que ya haya guardada una" +
                    " con el mismo nombre.\n" + resultadoGuardada.getMessage());
            }
        } else {
            Log.e(TAG, "No se ha podido guardar la imagen " + nombreImagen + " porque es nula.");
            imagenGuardada = false;
        }

        return imagenGuardada;
    }

    public static String getUniqueName(final Context context, final String imageName) {
        return new UniqueNameManager() {

            @Override
            public boolean isNombreRepetido(String nombre) {
                // TODO: 28/03/2019 ¿No habría que mirar aquí el nombre recibido?
                return isArchivoRepetido(context, imageName);
            }
        }.getUniqueName(imageName);
    }

    /**
     * Devuelve un booleano indicando si el archivo ya existe en el directorio <i>data/data/app/files</i>
     *
     * @param nombreArchivo El nombre del archivo en la carpeta <i>/data/data/nuestraApp/files</i>,
     * empezando sin barra. Por ejemplo: <i>imagen.jpg</i>.
     * @return boolean indicando si existe o no el archivo.
     */
    public static boolean isArchivoRepetido(Context context, String nombreArchivo) {

        final Path rutaImagen = DataManager.getInstance(context).getImagesDirectory()
            .file(nombreArchivo)
            .build();

        return MemoryUtil.exists(rutaImagen);
    }

    /**
     * Obtiene una imagen en forma de archivo <i>Bitmap</i> desde la carpeta assets
     *
     * @param nombreImagen Nombre de la imagen en la carpeta assets, empezando sin barra. Por ejemplo,
     * <i>imagen.jpg</i>.
     * @return La imagen convertida en <i>Bitmap</i>
     */
    public static Bitmap obtenerImagenDesdeAssets(Context context, String nombreImagen) {
        Result<Bitmap> cargarImagen = MemoryUtil.loadBitmapFromAssets(context, nombreImagen);
        return cargarImagen.getResult();
    }

    /**
     * Elimina un archivo de <i>data/data/nuestraApp/files</i>
     *
     * @param nombre Nombre del archivo que queremos borrar
     * @return Devuelve un booleano indicando si se ha borrado el archivo o no. Si no se ha borrado
     * es porque la ruta está mal indicada
     */
    public static boolean eliminarImagen(String nombre, Path.Builder ruta) {

        Path rutaImagen = ruta.file(nombre).build();

        if (!MemoryUtil.exists(rutaImagen)) {
            return true;
        } else {
            return MemoryUtil.deleteFile(rutaImagen, false).isSuccessful();
        }
    }

    /**
     * Convierte la imagen a un tamaño de 5cm (142px x 142px).
     *
     * @param bitmap <i>Bitmap</i> con la imagen que queremos redimensionar.
     * @returns Devuelve un bitmap escalado a 142px x 142px
     */
    public static Bitmap prepararBitmap(Bitmap bitmap) {
        Bitmap bitmapRecortado = null;

        // Primero la recorta el lado mas largo para que tenga un ratio de aspecto 1:1
        if (bitmap.getWidth() != bitmap.getHeight()) {
            int anchura = bitmap.getWidth();
            int altura = bitmap.getHeight();
            if (anchura < altura) {
                int alturaSobrante = altura - anchura;
                int y = alturaSobrante / 2;
                bitmapRecortado = Bitmap.createBitmap(bitmap, 0, y, anchura, anchura);
            } else if (altura < anchura) {
                int anchuraSobrante = anchura - altura;
                int x = anchuraSobrante / 2;
                bitmapRecortado = Bitmap.createBitmap(bitmap, x, 0, altura, altura);
            }
        } else {
            bitmapRecortado = bitmap;
        }

        //Escala la imagen a 142px y la devuelve
        return Bitmap.createScaledBitmap(bitmapRecortado, 142, 142, false);
    }
}
