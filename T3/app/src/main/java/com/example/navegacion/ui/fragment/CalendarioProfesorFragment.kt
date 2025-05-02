/*package com.example.navegacion.ui.fragment


import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentCalendarioprofesorBinding
import com.example.navegacion.ui.model.Evento
import com.example.navegacion.ui.adapter.EventosAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel



import android.content.Intent

import android.provider.CalendarContract

import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.CalendarView
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class CalendarioProfesorFragment : Fragment() {

    private var _binding: FragmentCalendarioprofesorBinding? = null
    private val binding get() = _binding!!

    private val calendarioViewModel: CalendarioViewModel by activityViewModels()
    private var selectedDate: Long = 0L
    private lateinit var eventosAdapter: EventosAdapter

    private val moduloActual = "NombreDelModulo" // TODO: cambiar por el modulo correspondiente

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarioprofesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarioViewModel.cargarEventosModuloDesdeFirebase(moduloActual)
        observarEventosDesdeViewModel()

        eventosAdapter = EventosAdapter(
            onEditar = { evento -> mostrarDialogoEvento(evento) },
            onEliminar = { evento -> calendarioViewModel.eliminarEventoModulo(selectedDate, evento, moduloActual) }
        )
        binding.recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEventos.adapter = eventosAdapter

        val currentMonth = YearMonth.now()
        val daysOfWeek = daysOfWeek()
        binding.calendarView.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        actualizarMesEnTexto(currentMonth)

        binding.calendarView.monthScrollListener = { month ->
            actualizarMesEnTexto(month.yearMonth)
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view).apply {
                    view.setOnClickListener {
                        val day = day
                        if (day.position == DayPosition.MonthDate) {
                            selectedDate = day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            actualizarListaEventos()
                            binding.calendarView.notifyDayChanged(day)
                        }
                    }
                }
            }

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                val dateTimestamp = day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val eventos = calendarioViewModel.obtenerEventosModulo().value?.get(dateTimestamp)

                when {
                    eventos.isNullOrEmpty() -> textView.setBackgroundResource(R.drawable.circle_default)
                    eventos.any { it.tipo == "Tarea" } -> textView.setBackgroundResource(R.drawable.circle_tarea)
                    eventos.any { it.tipo == "Examen" } -> textView.setBackgroundResource(R.drawable.circle_examen)
                    else -> textView.setBackgroundResource(R.drawable.circle_otro)
                }
            }
        }

        binding.btnAddEvent.setOnClickListener {
            mostrarDialogoEvento(null)
        }

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val fechaPasada = arguments?.getLong("fechaSeleccionada", 0L)
        selectedDate = if (fechaPasada != null && fechaPasada != 0L) {
            fechaPasada
        } else {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        actualizarListaEventos()
    }

    private fun actualizarMesEnTexto(month: YearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es"))
        binding.txtMes.text = month.format(formatter).replaceFirstChar { it.uppercaseChar() }
    }

    private fun actualizarListaEventos() {
        val eventos = calendarioViewModel.obtenerEventosModulo().value?.get(selectedDate) ?: emptyList()
        eventosAdapter.updateEventos(eventos)
    }

    private fun observarEventosDesdeViewModel() {
        calendarioViewModel.obtenerEventosModulo().observe(viewLifecycleOwner) { mapa ->
            val eventos = mapa[selectedDate] ?: emptyList()
            eventosAdapter.updateEventos(eventos)
        }
    }

    private fun mostrarDialogoEvento(eventoAEditar: Evento?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_evento, null)
        val editTitulo = dialogView.findViewById<EditText>(R.id.editTitulo)
        val editDescripcion = dialogView.findViewById<EditText>(R.id.editDescripcion)
        val spinnerTipo = dialogView.findViewById<Spinner>(R.id.spinnerTipo)

        val adapterSpinner = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tipos_evento,
            android.R.layout.simple_spinner_item
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adapterSpinner

        eventoAEditar?.let {
            editTitulo.setText(it.titulo)
            editDescripcion.setText(it.descripcion)
            val tipoIndex = adapterSpinner.getPosition(it.tipo)
            spinnerTipo.setSelection(tipoIndex)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (eventoAEditar == null) "Nuevo Evento" else "Editar Evento")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = editTitulo.text.toString()
                val descripcion = editDescripcion.text.toString()
                val tipo = spinnerTipo.selectedItem.toString()

                val nuevoEvento = Evento(
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    id = eventoAEditar?.id
                )

                if (eventoAEditar == null) {
                    calendarioViewModel.agregarEventoModulo(selectedDate, nuevoEvento, moduloActual)
                    abrirCalendarioConEvento(titulo, descripcion, selectedDate)
                } else {
                    calendarioViewModel.editarEventoModulo(selectedDate, eventoAEditar, nuevoEvento, moduloActual)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirCalendarioConEvento(titulo: String, descripcion: String, fecha: Long) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fecha)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fecha + 60 * 60 * 1000)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}*/
package com.example.navegacion.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentCalendarioprofesorBinding
import com.example.navegacion.ui.adapter.EventosAdapter
import com.example.navegacion.ui.model.Evento
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class CalendarioProfesorFragment : Fragment() {

    private var _binding: FragmentCalendarioprofesorBinding? = null
    private val binding get() = _binding!!

    private val calendarioViewModel: CalendarioViewModel by activityViewModels()
    private var selectedDate: Long = 0L
    private lateinit var eventosAdapter: EventosAdapter

    private var moduloActual: String? = null // <-- Ahora dinámico

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarioprofesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCalendar()

        // 🧠 Obtener automáticamente el módulo del profesor
        calendarioViewModel.obtenerModuloDelProfesor { modulo ->
            if (modulo != null) {
                moduloActual = modulo
                calendarioViewModel.cargarEventosModuloDesdeFirebase(modulo)
                observarEventosDesdeViewModel()
            }
        }

        binding.btnAddEvent.setOnClickListener {
            mostrarDialogoEvento(null)
        }

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val fechaPasada = arguments?.getLong("fechaSeleccionada", 0L)
        selectedDate = if (fechaPasada != null && fechaPasada != 0L) {
            fechaPasada
        } else {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        actualizarListaEventos()
    }

    private fun setupRecyclerView() {
        eventosAdapter = EventosAdapter(
            onEditar = { evento -> mostrarDialogoEvento(evento) },
            onEliminar = { evento ->
                moduloActual?.let { calendarioViewModel.eliminarEventoModulo(selectedDate, evento, it) }
            }
        )
        binding.recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEventos.adapter = eventosAdapter
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val daysOfWeek = daysOfWeek()
        binding.calendarView.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        actualizarMesEnTexto(currentMonth)

        binding.calendarView.monthScrollListener = { month ->
            actualizarMesEnTexto(month.yearMonth)
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view).apply {
                    view.setOnClickListener {
                        val day = day
                        if (day.position == DayPosition.MonthDate) {
                            selectedDate = day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            actualizarListaEventos()
                            binding.calendarView.notifyDayChanged(day)
                        }
                    }
                }
            }

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                val dateTimestamp = day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val eventos = calendarioViewModel.obtenerEventosModulo().value?.get(dateTimestamp)

                when {
                    eventos.isNullOrEmpty() -> textView.setBackgroundResource(R.drawable.circle_default)
                    eventos.any { it.tipo == "Tarea" } -> textView.setBackgroundResource(R.drawable.circle_tarea)
                    eventos.any { it.tipo == "Examen" } -> textView.setBackgroundResource(R.drawable.circle_examen)
                    else -> textView.setBackgroundResource(R.drawable.circle_otro)
                }
            }
        }
    }

    private fun actualizarMesEnTexto(month: YearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es"))
        binding.txtMes.text = month.format(formatter).replaceFirstChar { it.uppercaseChar() }
    }

    private fun actualizarListaEventos() {
        val eventos = calendarioViewModel.obtenerEventosModulo().value?.get(selectedDate) ?: emptyList()
        eventosAdapter.updateEventos(eventos)
    }

    private fun observarEventosDesdeViewModel() {
        calendarioViewModel.obtenerEventosModulo().observe(viewLifecycleOwner) { mapa ->
            val eventos = mapa[selectedDate] ?: emptyList()
            eventosAdapter.updateEventos(eventos)
        }
    }

    private fun mostrarDialogoEvento(eventoAEditar: Evento?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_evento, null)
        val editTitulo = dialogView.findViewById<EditText>(R.id.editTitulo)
        val editDescripcion = dialogView.findViewById<EditText>(R.id.editDescripcion)
        val spinnerTipo = dialogView.findViewById<Spinner>(R.id.spinnerTipo)

        val adapterSpinner = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tipos_evento,
            android.R.layout.simple_spinner_item
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adapterSpinner

        eventoAEditar?.let {
            editTitulo.setText(it.titulo)
            editDescripcion.setText(it.descripcion)
            val tipoIndex = adapterSpinner.getPosition(it.tipo)
            spinnerTipo.setSelection(tipoIndex)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (eventoAEditar == null) "Nuevo Evento" else "Editar Evento")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = editTitulo.text.toString()
                val descripcion = editDescripcion.text.toString()
                val tipo = spinnerTipo.selectedItem.toString()

                val nuevoEvento = Evento(
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    id = eventoAEditar?.id
                )

                moduloActual?.let { modulo ->
                    if (eventoAEditar == null) {
                        calendarioViewModel.agregarEventoModulo(selectedDate, nuevoEvento, modulo)
                        abrirCalendarioConEvento(titulo, descripcion, selectedDate)
                    } else {
                        calendarioViewModel.editarEventoModulo(selectedDate, eventoAEditar, nuevoEvento, modulo)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirCalendarioConEvento(titulo: String, descripcion: String, fecha: Long) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fecha)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fecha + 60 * 60 * 1000)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

