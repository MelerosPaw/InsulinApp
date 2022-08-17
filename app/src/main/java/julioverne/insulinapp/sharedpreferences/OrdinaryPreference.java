package julioverne.insulinapp.sharedpreferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import julioverne.insulinapp.R;

/**
 * Created by Juan José Melero on 01/06/2015.
 */
public class OrdinaryPreference extends Preference {

  @BindView(R.id.tv_titulo)
  TextView tvTitulo;
  @BindView(R.id.tv_summary)
  TextView tvSummary;

  public OrdinaryPreference(Context context) {
    super(context);
  }

  public OrdinaryPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public OrdinaryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onBindView(View view) {
    super.onBindView(view);
    ButterKnife.bind(this, view);
    loadView();
  }

  public void loadView() {
    tvTitulo.setText(getTitle());
    tvSummary.setText(getSummary());
  }
}
