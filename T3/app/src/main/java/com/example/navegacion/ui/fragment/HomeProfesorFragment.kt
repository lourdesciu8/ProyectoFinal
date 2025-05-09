package com.example.navegacion.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomeprofesorBinding
import com.example.navegacion.ui.adapter.ResumenEventosAdapter
import com.example.navegacion.ui.model.Calificacion
import com.example.navegacion.ui.utils.DecimalValueFormatter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeProfesorFragment : Fragment() {

    private var _binding: FragmentHomeprofesorBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()
    private lateinit var resumenAdapter: ResumenEventosAdapter
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeprofesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        setupRecyclerView()
        cargarNotasPorModuloYMostrarBarras()

        calendarioViewModel.cargarEventosProfesor(uid!!)
        calendarioViewModel.eventosProfesor.observe(viewLifecycleOwner) { lista ->
            val eventosOrdenados = lista.sortedBy { it.fecha }.take(5)
            resumenAdapter.updateEventos(eventosOrdenados)
        }
    }

    private fun setupRecyclerView() {
        resumenAdapter = ResumenEventosAdapter(mutableListOf()) { evento ->
            val bundle = Bundle().apply {
                putLong("fechaSeleccionada", evento.fecha ?: 0L)
            }
            findNavController().navigate(R.id.calendarioProfesorFragment, bundle)
        }

        binding.recyclerResumenEventosProfesor.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resumenAdapter
        }
    }

    private fun cargarNotasPorModuloYMostrarBarras() {
        val database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
        val currentProfesorUID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val califRef = database.getReference("calificaciones")

        califRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mapaModulos = mutableMapOf<String, MutableList<Double>>()

                for (alumnoSnap in snapshot.children) {
                    for (notaSnap in alumnoSnap.children) {
                        val calif = notaSnap.getValue(Calificacion::class.java) ?: continue
                        if (calif.profesorUID == currentProfesorUID && calif.tipo == "Examen") {
                            mapaModulos.getOrPut(calif.modulo ?: "Desconocido") { mutableListOf() }.add(calif.nota)
                        }
                    }
                }

                val modulos = mutableListOf<String>()
                val entries = mutableListOf<BarEntry>()
                var index = 0f

                for ((modulo, notas) in mapaModulos) {
                    val media = notas.average().toFloat()
                    modulos.add(modulo)
                    entries.add(BarEntry(index, media))
                    index += 1f
                    Log.d("GRAFICO", "$modulo: media $media")
                }

                mostrarGraficoBarras(entries, modulos)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GRAFICO", " Error al cargar notas: ${error.message}")
            }
        })
    }

    private fun mostrarGraficoBarras(entries: List<BarEntry>, labels: List<String>) {
        val context = requireContext()

        val moduloColors = listOf(
            ContextCompat.getColor(context, R.color.cal_poly_green),
            ContextCompat.getColor(context, R.color.shamrock_green),
            ContextCompat.getColor(context, R.color.verdigris),
            ContextCompat.getColor(context, R.color.turquoise),
            ContextCompat.getColor(context, R.color.aqua)
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
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -30f
                textSize = 12f
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
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
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
        popupMenu.menuInflater.inflate(R.menu.popup_menu_profesor, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_foro -> {
                    findNavController().navigate(R.id.action_homeProfesor_to_hilosFragment)
                    true
                }
                R.id.action_calendario_profesor -> {
                    findNavController().navigate(R.id.calendarioProfesorFragment)
                    true
                }
                R.id.action_calificaciones_profesor -> {
                    findNavController().navigate(R.id.calificacionesProfesorFragment)
                    true
                }
                R.id.action_temario_profesor -> {
                    findNavController().navigate(R.id.temarioProfesorFragment)
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
