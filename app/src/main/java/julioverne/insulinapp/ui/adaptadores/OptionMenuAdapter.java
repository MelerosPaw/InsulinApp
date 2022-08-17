package julioverne.insulinapp.ui.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import julioverne.insulinapp.R;
import julioverne.insulinapp.constants.Constants;
import julioverne.insulinapp.databinding.ItemOpcionSimpleLayoutBinding;

/**
 * Created by Juan José Melero on 20/06/2015.
 */
public class OptionMenuAdapter extends ArrayAdapter<String> {

    public OptionMenuAdapter(
        @NonNull final Context context,
        final int textViewResourceId,
        @NonNull final String[] options
    ) {
        super(context, textViewResourceId, options);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View item = convertView;
        final ViewHolder holder;

        if (item == null) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final ItemOpcionSimpleLayoutBinding binding = ItemOpcionSimpleLayoutBinding.inflate(
                inflater, parent, false);
            item = binding.getRoot();
            holder = new ViewHolder(item, binding);
            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }

        holder.bind(getItem(position));
        return item;
    }

    private static class ViewHolder {

        @NonNull
        private final View itemView;
        @NonNull
        private final ItemOpcionSimpleLayoutBinding binding;

        ViewHolder(@NonNull final View itemView, @NonNull final ItemOpcionSimpleLayoutBinding binding) {
            this.itemView = itemView;
            this.binding = binding;
        }

        private void bind(@NonNull final String option) {
            binding.setOptionName(option);
            setUpIcon(option);
        }

        private void setUpIcon(@NonNull final String option) {

            @DrawableRes final int drawable;

            switch (option) {
                case Constants.REALIZAR_CONTROL:
                    drawable = R.mipmap.ic_control;
                    break;
                case Constants.DIARIO:
                    drawable = R.mipmap.ic_diario;
                    break;
                case Constants.ALIMENTOS:
                    drawable = R.mipmap.ic_alimentos;
                    break;
                case Constants.INSULINA_ACTUAL:
                    drawable = R.mipmap.ic_actual;
                    break;
                case Constants.ALARMAS:
                default:
                    drawable = R.mipmap.ic_alarma;
                    break;
            }

            binding.setIcon(drawable);
        }
    }
}
