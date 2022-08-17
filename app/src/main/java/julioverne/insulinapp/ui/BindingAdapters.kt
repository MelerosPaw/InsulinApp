package julioverne.insulinapp.ui

import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("adapter")
fun setAdapter(listView: ListView, adapter: ArrayAdapter<*>) {
    listView.adapter = adapter
}

@BindingAdapter("adapter")
fun setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    recyclerView.adapter = adapter
}

@BindingAdapter("onItemClick")
fun setOnItemClick(listView: ListView, listener: AdapterView.OnItemClickListener) {
    listView.onItemClickListener = listener
}

@BindingAdapter("textWatcher")
fun addTextChangedListener(editText: EditText, watcher: TextWatcher?) {
    if (watcher != null) {
        editText.addTextChangedListener(watcher)
    }
}

@BindingAdapter("visible")
fun isVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}