package julioverne.insulinapp.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import julioverne.insulinapp.R;

/**
 * Created by Juan José Melero on 25/04/2015.
 */
public class PreferenciasFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
    }

    //Si ponemos un layout en el que haya un ListView con android:id="@id/android:list", la pantalla
    //principal de las preferencias sale así
    //    @Override
    //    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    //        return inflater.inflate(R.layout.preferencias_layout, container, false);
    //    }
}
