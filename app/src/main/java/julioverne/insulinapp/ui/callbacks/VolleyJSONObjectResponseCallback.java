package julioverne.insulinapp.ui.callbacks;

import com.android.volley.VolleyError;
import org.json.JSONObject;

/**
 * Created by Juan José Melero on 09/06/2015.
 */
public interface VolleyJSONObjectResponseCallback {

    void onSuccess(JSONObject jsonArray);

    void onFailure(VolleyError volleyError);
}
