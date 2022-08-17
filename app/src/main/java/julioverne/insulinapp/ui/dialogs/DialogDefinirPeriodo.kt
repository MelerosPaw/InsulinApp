package julioverne.insulinapp.ui.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import julioverne.insulinapp.R
import julioverne.insulinapp.ui.viewmodels.ViewModelPeriodos
import julioverne.insulinapp.utils.DateUtils
import julioverne.insulinapp.utils.hour
import julioverne.insulinapp.utils.minute
import julioverne.insulinapp.utils.toast
import java.util.*

class DialogDefinirPeriodo : DialogFragment() {

    companion object {
        const val TAG = "DialogDefinirPeriodo"
    }

    @BindView(R.id.cl_root)
    lateinit var clRoot: ConstraintLayout
    @BindView(R.id.tv_titulo_definir_periodo)
    lateinit var tvTitulo: TextView
    @BindView(R.id.tv_mensaje_definir_periodo)
    lateinit var tvMensaje: TextView

    //    @BindView(R.id.picker_tiempo)               lateinit var tpHoras: TimePicker
//    @BindView(R.id.tv_hora_inicio)              lateinit var tvHoraInicio: TextView
//    @BindView(R.id.tv_guion)                    lateinit var tvGuion: TextView
//    @BindView(R.id.tv_hora_fin)                 lateinit var tvHoraFin: TextView
    @BindView(R.id.btn_cancelar)
    lateinit var btnCancelar: TextView
    @BindView(R.id.btn_definir)
    lateinit var btnDefinir: TextView

    private val viewModel: ViewModelPeriodos by lazy {
        ViewModelProviders.of(this).get(ViewModelPeriodos::class.java)
    }
    private val observerHoraInicio = Observer<Calendar> { onHoraSeleccionada(it) }
    private var unbinder: Unbinder? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_definir_periodo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view)
        cargarVista()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder?.unbind()
    }

    private fun cargarVista() {
        viewModel.horaInicio.observe(viewLifecycleOwner, observerHoraInicio)
        setHora(viewModel.horaInicio.value, true)
        setHora(viewModel.horaFin.value, false)
        pintarHora(viewModel.horaInicio.value)
//        tpHoras.setIs24HourView(true)
    }

    private fun obtenerHora(horas: Int? = null, minutos: Int? = null): Calendar {

        val hora = Calendar.getInstance()

        if (horas != null && minutos != null) {
            hora.hour = horas
            hora.minute = minutos

        } else {
            if (isMarshMallow()) {
//                hora.time = obtenerHora(tpHoras.hour, tpHoras.minute).time
            } else {
//                hora.time = obtenerHora(tpHoras.currentHour, tpHoras.currentMinute).time
            }
        }

        return hora
    }

    private fun setHora(hora: Calendar?, esHoraInicio: Boolean) {
        hora?.let {
            val horaFormateada = DateUtils.dateToHour(it.time)

            if (esHoraInicio) {
//                tvHoraInicio.text = horaFormateada
            } else {
//                tvHoraFin.text = horaFormateada
            }
        }
    }

    private fun onHoraSeleccionada(hora: Calendar?) {
        prepararAnimacion()
        pintarHora(hora)
    }

    private fun pintarHora(hora: Calendar?) {
        if (hora != null) {
            pintarConHoraDeInicio(hora)

        } else {
            pintarSinHoraInicio()
        }
    }

    private fun pintar(
        @StringRes titulo: Int, @StringRes mensaje: Int, @StringRes definir: Int,
        @StringRes cancelar: Int, listenerDefinir: () -> Unit,
        listenerCancelar: () -> Unit
    ) {

        tvTitulo.setText(titulo)
        tvMensaje.setText(mensaje)
        btnDefinir.setText(definir)
        btnCancelar.setText(cancelar)
        btnDefinir.setOnClickListener { listenerDefinir() }
        btnCancelar.setOnClickListener { listenerCancelar() }
    }

    private fun pintarSinHoraInicio() {
        pintar(R.string.hora_inicio_periodo,
            R.string.hora_inicio_periodo, R.string.definir, R.string.cancelar,
            { viewModel.horaInicio.value = obtenerHora() },
            { dismiss() })
//        invisible(true, tvGuion, tvHoraFin)
        setHora(null, false)
    }

    private fun pintarConHoraDeInicio(hora: Calendar?) {
        pintar(R.string.hora_inicio_periodo, R.string.hora_fin_periodo,
            R.string.finalizar, R.string.atras,
            {
                viewModel.horaFin.value = obtenerHora()
                activity?.toast("Tengo cosas")
            },
            { viewModel.horaInicio.value = null })
//        visible(true, tvGuion, tvHoraFin)
        setHora(hora, true)
    }

    private fun prepararAnimacion() {
        if (isMarshMallow()) {
            TransitionManager.beginDelayedTransition(clRoot)
        }
    }

    private fun isMarshMallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
}