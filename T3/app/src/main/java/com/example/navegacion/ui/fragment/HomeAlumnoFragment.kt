package com.example.navegacion.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomealumnoBinding
import com.example.navegacion.ui.adapter.ResumenEventosAdapter
import com.example.navegacion.ui.model.Calificacion
import com.example.navegacion.ui.model.Evento
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.navegacion.ui.utils.DecimalValueFormatter

class HomeAlumnoFragment : Fragment() {

    private var _binding: FragmentHomealumnoBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()
    private lateinit var resumenAdapter: ResumenEventosAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomealumnoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNotificaciones.setOnClickListener {
            findNavController().navigate(R.id.action_homeAlumnoFragment_to_notificacionesFragment)
        }

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        setupRecyclerView()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        calendarioViewModel.cargarEventosAlumno(uid)
        calendarioViewModel.eventosAlumno.observe(viewLifecycleOwner) { lista ->
            // Filtrar eventos asignados al alumno y también los que él mismo creó como personales
            val eventosFiltrados = lista.filter {
                it.asignadoA?.containsKey(uid) == true || (it.creadoPor == uid && it.esPersonal == true)
            }
            val eventosOrdenados = eventosFiltrados
                .sortedBy { it.fecha }
                .take(5)
            resumenAdapter.updateEventos(eventosOrdenados)
        }

        cargarNotasPorModuloYMostrarBarras()
    }

    private fun setupRecyclerView() {
        resumenAdapter = ResumenEventosAdapter(mutableListOf()) { evento ->
            val bundle = Bundle().apply {
                putLong("fechaSeleccionada", evento.fecha ?: 0L)
            }
            findNavController().navigate(R.id.calendarioAlumnoFragment, bundle)
        }

        binding.recyclerResumenEventos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resumenAdapter
        }
    }

    private fun cargarNotasPorModuloYMostrarBarras() {
        val database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = database.getReference("calificaciones").child(uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mapaModulos = mutableMapOf<String, MutableList<Double>>()

                for (moduloSnap in snapshot.children) {
                    val nombreModulo = moduloSnap.key ?: continue
                    for (notaSnap in moduloSnap.children) {
                        val calif = notaSnap.getValue(Calificacion::class.java)
                        if (calif?.tipo == "Examen") {
                            mapaModulos.getOrPut(nombreModulo) { mutableListOf() }.add(calif.nota)
                        }
                    }
                }

                val modulos = mutableListOf<String>()
                val entries = mutableListOf<BarEntry>()
                var index = 0f

                for ((modulo, notas) in mapaModulos) {
                    val media = notas.average().toFloat()
                    entries.add(BarEntry(index, media))
                    modulos.add(modulo)
                    index += 1f
                }

                mostrarGraficoBarras(entries, modulos)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarGraficoBarras(entries: List<BarEntry>, labels: List<String>) {
        val context = requireContext()

        val moduloColors = listOf(
            ContextCompat.getColor(context, R.color.primaryLight),       // #5BB3C2
            ContextCompat.getColor(context, R.color.primaryDarker),     // #A7DBE5
            ContextCompat.getColor(context, R.color.primaryDark),        // #2B7683
            ContextCompat.getColor(context, R.color.orange),         // #A95739
            ContextCompat.getColor(context, R.color.colorTextSecondary)         // #A99D39
        )


        val usedColors = labels.mapIndexed { index, _ ->
            moduloColors.getOrElse(index) { Color.GRAY }
        }

        val dataSet = BarDataSet(entries, "").apply {
            colors = usedColors
            valueTextSize = 14f
            valueFormatter = DecimalValueFormatter()
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.4f
        }

        binding.barChart.apply {
            data = barData
            description.isEnabled = false
            animateY(1000)
            setFitBars(true)

            xAxis.apply {
                setDrawLabels(false)
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            }

            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
            axisRight.isEnabled = false

            legend.isEnabled = true
            legend.setCustom(
                labels.mapIndexed { index, label ->
                    LegendEntry().apply {
                        formColor = usedColors[index]
                        this.label = label
                    }
                }
            )
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                xEntrySpace = 10f
                yEntrySpace = 8f
                textSize = 14f
            }

            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = labels.size - 0.5f

            invalidate()
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_alumno, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_foro -> {
                    findNavController().navigate(R.id.action_homeAlumno_to_hilosFragment)
                    true
                }
                R.id.action_calendario -> {
                    findNavController().navigate(R.id.calendarioAlumnoFragment)
                    true
                }
                R.id.action_progreso -> {
                    findNavController().navigate(R.id.action_homeAlumnoFragment_to_progresoFragment)
                    true
                }
                R.id.action_temario -> {
                    findNavController().navigate(R.id.action_homeAlumnoFragment_to_temarioAlumnoFragment)
                    true
                }
                R.id.action_calificaciones -> {
                    findNavController().navigate(R.id.action_homeAlumnoFragment_to_calificacionesAlumnoFragmnet)
                    true
                }
                R.id.action_logout -> {
                    findNavController().navigate(R.id.loginFragment)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

