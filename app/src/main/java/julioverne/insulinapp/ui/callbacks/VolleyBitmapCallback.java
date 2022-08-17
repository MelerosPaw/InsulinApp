package julioverne.insulinapp.ui.callbacks;

import android.graphics.Bitmap;
import com.android.volley.VolleyError;

/**
 * Created by Juan José Melero on 09/06/2015.
 */
public interface VolleyBitmapCallback {

    void onSuccess(Bitmap bitmap);

    void onFailure(VolleyError volleyError);
}
