package julioverne.insulinapp.ui.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import julioverne.insulinapp.utils.TypefacesUtils;

/**
 * Created by Juan José Melero on 19/06/2015.
 */
public class BaseFragment extends Fragment {

  protected Typeface light;
  protected Typeface negrita;
  protected Typeface cursiva;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    light = TypefacesUtils.get(getActivity(), "fonts/DejaVuSans-ExtraLight.ttf");
    negrita = TypefacesUtils.get(getActivity(), "fonts/DejaVuSansCondensed-Bold.ttf");
    cursiva = TypefacesUtils.get(getActivity(), "fonts/DejaVuSansCondensed-Oblique.ttf");
  }
}
