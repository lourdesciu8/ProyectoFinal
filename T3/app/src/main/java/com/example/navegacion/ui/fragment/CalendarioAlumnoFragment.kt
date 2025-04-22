/*
package com.example.navegacion.ui.fragment

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

        // Cargar datos desde Firebase
        calendarioViewModel.cargarEventosDesdeFirebase()

        // Observar los cambios del LiveData (muy importante para que se actualice la lista)
        observarEventosDesdeViewModel()

        eventosAdapter = EventosAdapter(
            onEditar = { evento -> mostrarDialogoEvento(evento) },
            onEliminar = { evento ->
                calendarioViewModel.eliminarEvento(selectedDate, evento)
                // Ya no hace falta llamar a actualizarListaEventos() porque el LiveData observará los cambios
            }
        )


        binding.recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEventos.adapter = eventosAdapter

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = convertToTimestamp(year, month, dayOfMonth)
            actualizarListaEventos()
        }

        binding.btnAddEvent.setOnClickListener {
            mostrarDialogoEvento(null) // Nuevo evento
        }

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Mostrar eventos del día actual al abrir
        selectedDate = convertToTimestamp(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        actualizarListaEventos()
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
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun convertToDateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
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
                    id = eventoAEditar?.id // conservar el ID si estamos editando
                )

                if (eventoAEditar == null) {
                    calendarioViewModel.agregarEvento(selectedDate, nuevoEvento)
                } else {
                    calendarioViewModel.editarEvento(selectedDate, eventoAEditar, nuevoEvento)
                }

                // Ya no hace falta llamar a actualizarListaEventos()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

*/
package com.example.navegacion.ui.fragment

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
import java.util.Calendar

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

        // Cargar eventos desde Firebase
        calendarioViewModel.cargarEventosDesdeFirebase()

        // Observar cambios en los eventos
        observarEventosDesdeViewModel()

        eventosAdapter = EventosAdapter(
            onEditar = { evento -> mostrarDialogoEvento(evento) },
            onEliminar = { evento ->
                calendarioViewModel.eliminarEvento(selectedDate, evento)
                // No hace falta actualizar manualmente, LiveData lo hace
            }
        )

        binding.recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEventos.adapter = eventosAdapter

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = convertToTimestamp(year, month, dayOfMonth)
            actualizarListaEventos()
        }

        binding.btnAddEvent.setOnClickListener {
            mostrarDialogoEvento(null)
        }

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Detectar si se pasa una fecha desde otro fragmento
        val fechaPasada = arguments?.getLong("fechaSeleccionada", 0L)
        if (fechaPasada != null && fechaPasada != 0L) {
            selectedDate = fechaPasada
            binding.calendarView.date = selectedDate
        } else {
            selectedDate = convertToTimestamp(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
        }

        actualizarListaEventos()
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
}
