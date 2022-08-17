package julioverne.insulinapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import java.util.Hashtable;

/**
 * Created by Juan José Melero on 12/05/2015.
 */

public class TypefacesUtils {

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    /**
     * Devuelve una fuente de la carpeta <i>app/assets</i> de nuestro proyecto
     * y la guarda en una <i>Hashtable</i>. Para devolverla de ahí la próxima vez sin causar un
     * error ni consumir más memoria de la cuenta.
     *
     * @param context Context desde donde estamos solicitando la tipografía.
     * @param fontName Nombre de la fuente, que debe estar almacenada en <i>assets</i>. Si está
     * dentro de una carpeta en <i>assets</i>, el nombre debe indicar la ruta. P.e.,
     * si está en <i>assets/fonts</i>, este parámetro tiene que valer "fonts/nombreFuente".
     * @return Devuelve el objeto Typeface con la tipografía solicitada
     */
    public static Typeface get(Context context, String fontName) {
        synchronized (cache) {
            if (!cache.containsKey(fontName)) {
                try {
                    Typeface t = Typeface.createFromAsset(context.getAssets(),
                        fontName);
                    cache.put(fontName, t);
                } catch (Exception e) {
                    System.out.println("Could not get typeface '" + fontName
                        + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(fontName);
        }
    }
}
