package julioverne.insulinapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import julioverne.insulinapp.ui.callbacks.VolleyBitmapCallback;
import julioverne.insulinapp.ui.callbacks.VolleyJSONObjectResponseCallback;
import org.json.JSONObject;

public class VolleyUtils {

  public static RequestQueue requestQueue;

  /**
   * Devuelve una misma instancia de la cola de peticiones de Volley.
   *
   * @param context Contexto. Para asegurarse de que el contexto es el mismo siempre, realiza el
   * método {@code getAplicationContext()} sobre el contexto recibido.
   */
  public static RequestQueue getRequestQueue(Context context) {
      if (requestQueue == null) {
          requestQueue = Volley.newRequestQueue(context.getApplicationContext());
      }

    return requestQueue;
  }

  /**
   * Pone en la cola de Volley una petición GET de un {@code JSONArray(JsonArrayResponse)} y
   * requiere de un {@code VolleyResponseCallback} para recibir la respuesta o un
   * {@code VolleyError}.
   *
   * @param context Contexto que se usará para obtener la instancia <i>singleton</i> de la
   * {@code RequestQueue}.
   * @param url Direccion URL con los parámetros concatenados al final como atributos.
   * Ejemplo: "http://www.ejemplo.es?parametro=valor". Si tiene espacios,
   * el método los sustituye previamente por %20.
   * @param tag Cualquier {@code Object} que sirva como tag. Se recomienda que sea un String,
   * ya que se puede crear en cualquier momento. Además, al cancelar, se comparan
   * el tag indicado con el tag del objeto con ==, y con {@code String} no da
   * problemas.
   * @param callback {@code new VolleyResponseCallback} que devolverá un {@code JSONArray} en caso
   * de que el servicio se realize correctamente o un {@code VolleyError} en caso
   * contrario, al cual se le debe hacer {@code toString()} para conocer el
   * motivo.
   */
  public static void makeRequest(Context context, String url, Object tag,
      final VolleyJSONObjectResponseCallback callback) {

    //Elimina los espacios para que sea un URL válida
    url = url.replaceAll(" ", "%20");
    JsonObjectRequest request = new JsonObjectRequest(url, null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            callback.onSuccess(response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            callback.onFailure(error);
          }
        });

    addToQueue(context, request, tag);
  }

  /**
   * Añade un objeto a la cola usando como tag el objeto que se le pase. Establece una
   * política de 3 reintentos si no responde a los 2,5 segundos {@code (DEFAULT_TIMEOUT_MS)}, el servicio
   * puede llegar a reintentarse hasta 3 veces.
   *
   * @param context Preferiblemente una {@code Activity}, ya que si le pasamos la aplicación no podremos
   * distinguirlo a la hora de cancelar peticiones.
   * @param request Petición que queremos encolar.
   */
  public static void addToQueue(Context context, Request request, Object tag) {

    request.setTag(tag);
    //Política de reintento
    //        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    getRequestQueue(context).add(request);
  }

  /**
   * Cancela todos los servicios web iniciados desde una determinada {@code Activity}.
   *
   * @param tag Tag que se ha usado para iniciar la petición.
   */
  public static void cancelQueue(Context context, Object tag) {
    getRequestQueue(context.getApplicationContext()).cancelAll(tag);
  }

  public static void imageRequest(Context context, String url, Object tag,
      final VolleyBitmapCallback callback) {
    ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
      @Override
      public void onResponse(Bitmap response) {
        callback.onSuccess(response);
      }
    }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        callback.onFailure(error);
      }
    });

    addToQueue(context, imageRequest, tag);
  }
}