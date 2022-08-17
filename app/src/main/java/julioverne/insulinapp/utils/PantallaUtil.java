package julioverne.insulinapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Juan José Melero on 06/01/2017.
 */

public class PantallaUtil {

  /**
   * This method converts dp unit to equivalent pixels, depending on device density.
   *
   * @param dp A value in dp (density independent pixels) unit. Which we need
   * to convert into pixels.
   * @param context Context to get resources and device specific display metrics.
   * @return A float value to represent px equivalent to dp depending on device
   * density.
   */
  public static float convertirDPEnPixeles(float dp, Context context) {
    Resources resources = context.getResources();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    return px;
  }

  /**
   * This method converts device specific pixels to density independent pixels.
   *
   * @param px A value in px (pixels) unit. Which we need to convert into db.
   * @param context Context to get resources and device specific display metrics.
   * @return A float value to represent dp equivalent to px value.
   */
  public static float convertirPixelesEnDP(float px, Context context) {
    Resources resources = context.getResources();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    return dp;
  }
}
