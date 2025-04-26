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
import com.example.navegacion.databinding.FragmentCalendarioalumnoBinding
import com.example.navegacion.ui.model.Evento
import com.example.navegacion.ui.adapter.EventosAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.gestures.Orientation
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import java.time.format.DateTimeFormatter
import com.kizitonwose.calendar.core.CalendarMonth



class CalendarioAlumnoFragment : Fragment() {
    private var _binding: FragmentCalendarioalumnoBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()
    private var selectedDate: Long = 0L

    private lateinit var eventosAdapter: EventosAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarioalumnoBinding.inflate(inflater, container, false)
        return binding.root
    }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)

      // 🔄 Sincronizar eventos desde Firebase
      calendarioViewModel.cargarEventosDesdeFirebase()
      observarEventosDesdeViewModel()

      // 🎯 Adaptador para la lista de eventos
      eventosAdapter = EventosAdapter(
          onEditar = { evento -> mostrarDialogoEvento(evento) },
          onEliminar = { evento -> calendarioViewModel.eliminarEvento(selectedDate, evento) }
      )
      binding.recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())
      binding.recyclerViewEventos.adapter = eventosAdapter

      // 📅 Setup del calendario mensual
      val currentMonth = YearMonth.now()
      val daysOfWeek = daysOfWeek()
      binding.calendarView.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
      binding.calendarView.scrollToMonth(currentMonth)

      // 🆕 Mostrar el mes actual al iniciar
      actualizarMesEnTexto(currentMonth)

      // 📆 Cambiar el mes al hacer scroll horizontal
      binding.calendarView.monthScrollListener = { month ->
          actualizarMesEnTexto(month.yearMonth)
      }



      // 📌 Acción al hacer clic en un día
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
              val eventos = calendarioViewModel.obtenerEventos().value?.get(dateTimestamp)

              when {
                  eventos.isNullOrEmpty() -> textView.setBackgroundResource(R.drawable.circle_default)
                  eventos.any { it.tipo == "Tarea" } -> textView.setBackgroundResource(R.drawable.circle_tarea)
                  eventos.any { it.tipo == "Examen" } -> textView.setBackgroundResource(R.drawable.circle_examen)
                  else -> textView.setBackgroundResource(R.drawable.circle_otro)
              }
          }
      }

      // ➕ Botón para añadir evento
      binding.btnAddEvent.setOnClickListener {
          mostrarDialogoEvento(null)
      }

      // 🔙 Volver atrás
      binding.btnVolver.setOnClickListener {
          requireActivity().onBackPressed()
      }

      // 📩 Comprobar si se pasó una fecha desde otro fragmento
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
        val eventos = calendarioViewModel.obtenerEventos().value?.get(selectedDate) ?: emptyList()
        eventosAdapter.updateEventos(eventos)
    }

    private fun observarEventosDesdeViewModel() {
        calendarioViewModel.obtenerEventos().observe(viewLifecycleOwner) { mapa ->
            val eventos = mapa[selectedDate] ?: emptyList()
            eventosAdapter.updateEventos(eventos)
        }
    }

    private fun convertToTimestamp(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
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
                    calendarioViewModel.agregarEvento(selectedDate, nuevoEvento)
                    abrirCalendarioConEvento(titulo, descripcion, selectedDate)
                } else {
                    calendarioViewModel.editarEvento(selectedDate, eventoAEditar, nuevoEvento)
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
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fecha + 60 * 60 * 1000) // 1h de duración
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
import com.example.navegacion.databinding.FragmentCalendarioalumnoBinding
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
import java.util.*

class CalendarioAlumnoFragment : Fragment() {

    private var _binding: FragmentCalendarioalumnoBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()
    private var selectedDate: Long = 0L

    private lateinit var eventosAdapter: EventosAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarioalumnoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        calendarioViewModel.obtenerModuloDelProfesor { modulo ->
            if (modulo != null) {
                calendarioViewModel.cargarEventosDesdeFirebase()
                calendarioViewModel.cargarEventosModuloDesdeFirebase(modulo)
                observarEventosCombinados()
            } else {
                // Error en obtener módulo
            }
        }

        setupCalendar()

        binding.btnAddEvent.setOnClickListener {
            mostrarDialogoEvento(null)
        }

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val fechaPasada = arguments?.getLong("fechaSeleccionada", 0L)
        selectedDate = fechaPasada.takeIf { it != 0L } ?: Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        actualizarListaEventos()
    }

    private fun setupRecyclerView() {
        eventosAdapter = EventosAdapter(
            onEditar = { evento -> mostrarDialogoEvento(evento) },
            onEliminar = { evento -> calendarioViewModel.eliminarEvento(selectedDate, evento) }
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
                val eventosPersonales = calendarioViewModel.obtenerEventos().value?.get(dateTimestamp) ?: emptyList()
                val eventosModulo = calendarioViewModel.obtenerEventosModulo().value?.get(dateTimestamp) ?: emptyList()
                val eventos = eventosPersonales + eventosModulo

                when {
                    eventos.isEmpty() -> textView.setBackgroundResource(R.drawable.circle_default)
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
        val eventosPersonales = calendarioViewModel.obtenerEventos().value?.get(selectedDate) ?: emptyList()
        val eventosModulo = calendarioViewModel.obtenerEventosModulo().value?.get(selectedDate) ?: emptyList()

        val eventosCombinados = (eventosPersonales + eventosModulo).sortedBy { it.timestamp }
        eventosAdapter.updateEventos(eventosCombinados)
    }

//funcion para cargar tantos mis eventos como los que me asigan el profesor
    private fun observarEventosCombinados() {
        calendarioViewModel.obtenerEventos().observe(viewLifecycleOwner) {
            actualizarListaEventos()
        }
        calendarioViewModel.obtenerEventosModulo().observe(viewLifecycleOwner) {
            actualizarListaEventos()
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
                    calendarioViewModel.agregarEvento(selectedDate, nuevoEvento)
                    abrirCalendarioConEvento(titulo, descripcion, selectedDate)
                } else {
                    calendarioViewModel.editarEvento(selectedDate, eventoAEditar, nuevoEvento)
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

