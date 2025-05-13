package com.example.navegacion.ui.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import com.example.navegacion.R
import com.example.navegacion.ui.model.Calificacion
import com.example.navegacion.databinding.FragmentProgresoBinding
import com.github.mikephil.charting.components.Legend
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


class ProgresoFragment : Fragment() {
    private var _binding: FragmentProgresoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgresoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        cargarCalificacionesDesdeFirebase { calificaciones ->
            val total = calificaciones.size
            val examenes = calificaciones.count { it.tipo == "Examen" }
            val ejercicios = calificaciones.count { it.tipo == "Ejercicio" }

            Toast.makeText(
                requireContext(),
                "Total: $total\nExámenes: $examenes\nEjercicios: $ejercicios",
                Toast.LENGTH_LONG
            ).show()
            //mostramos graficos por aqui
            mostrarBarChartEjercicios(calificaciones)
            mostrarMediaGlobalExamenes(calificaciones)
            mostrarBarChartExamenes(calificaciones)
        }
    }
    //ya que tenemos una paleta de colores la he usado de mas oscuro a mas claro de suspenso a 10
    private fun obtenerColorNota(context: Context, nota: Float): Int {
        return when {
            nota < 0f -> ContextCompat.getColor(context, R.color.primaryDark)       // #2B7683 – tono profundo
            nota < 5f -> ContextCompat.getColor(context, R.color.accentMuted)       // #A99D39 – mostaza envejecido
            nota < 6.5f -> ContextCompat.getColor(context, R.color.orange)      // #A95739 – terracota
            nota < 9.5f -> ContextCompat.getColor(context, R.color.primaryLight)    // #5BB3C2 – tono más claro del principal
            else -> ContextCompat.getColor(context, R.color.colorApp)               // #3998A9 – color principal
        }

    }
    private fun mostrarBarChartExamenes(lista: List<Calificacion>) {
        val examenes = lista.filter { it.tipo == "Examen" }
        val modulosMap = mutableMapOf<String, MutableList<Double>>()
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        var index = 0f

        for (cal in examenes) {
            modulosMap.getOrPut(cal.modulo) { mutableListOf() }.add(cal.nota)
        }

        for ((modulo, notas) in modulosMap) {
            val media = notas.average().toFloat()
            entries.add(BarEntry(index, media))
            labels.add(modulo)
            index += 1f
        }

        val dataSet = BarDataSet(entries, "").apply {
            colors = entries.map { entry ->
                obtenerColorNota(requireContext(), entry.y)
            }
            valueTextSize = 14f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.4f
        }

        binding.barChartExamenes.apply {
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
                labelRotationAngle = 0f
                textSize = 12f
            }
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                xEntrySpace = 10f
                yEntrySpace = 8f
                textSize = 14f
            }

            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
            axisRight.isEnabled = false

            legend.isEnabled = true
            legend.setCustom(
                labels.mapIndexed { i, label ->
                    LegendEntry().apply {
                        formColor = dataSet.colors[i % dataSet.colors.size]
                        this.label = label
                    }
                }
            )

            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = labels.size - 0.5f

            invalidate()
        }
    }
    private fun mostrarBarChartEjercicios(lista: List<Calificacion>) {
        val ejercicios = lista.filter { it.tipo == "Ejercicio" }

        val modulosMap = mutableMapOf<String, MutableList<Double>>()

        for (cal in ejercicios) {
            modulosMap.getOrPut(cal.modulo) { mutableListOf() }.add(cal.nota)
        }

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        var index = 0f

        for ((modulo, notas) in modulosMap) {
            val media = notas.average().toFloat()
            entries.add(BarEntry(index, media))
            labels.add(modulo)
            index += 1f
        }

        val dataSet = BarDataSet(entries, "").apply {
            colors = entries.map { entry ->
                obtenerColorNota(requireContext(), entry.y)
            }
            valueTextSize = 14f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.4f
        }

        binding.barChartEjercicios.apply {
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
                labelRotationAngle = 0f
                textSize = 12f
            }
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                xEntrySpace = 10f
                yEntrySpace = 8f
                textSize = 14f
            }

            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
            axisRight.isEnabled = false

            legend.isEnabled = true
            legend.setCustom(
                labels.mapIndexed { i, label ->
                    LegendEntry().apply {
                        formColor = dataSet.colors[i % dataSet.colors.size]
                        this.label = label
                    }
                }
            )

            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = labels.size - 0.5f

            invalidate()
        }
    }

    private fun mostrarMediaGlobalExamenes(lista: List<Calificacion>) {
        val examenes = lista.filter { it.tipo == "Examen" }
        if (examenes.isEmpty()) return

        val promedio = examenes.map { it.nota }.average().coerceIn(0.0, 10.0)
        val porcentaje = promedio / 10.0 * 100
        val restante = 100 - porcentaje
        val colorMedia= obtenerColorNota(requireContext(),promedio.toFloat())
        val entries = listOf(
            PieEntry(porcentaje.toFloat(), "Media"),
            PieEntry(restante.toFloat())
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colorMedia, Color.LTGRAY)
            valueTextSize = 16f
        }

        val data = PieData(dataSet)

        binding.pieChartGlobal.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = false
            centerText = "Media Exámenes\n%.1f".format(promedio)
            setCenterTextSize(18f)
            this.data = data
            animateY(1400)
            invalidate()
        }
    }
    private fun cargarCalificacionesDesdeFirebase(onResult: (List<Calificacion>) -> Unit) {
        val database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
        val ref = database.getReference("calificaciones")

        val lista = mutableListOf<Calificacion>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (usuarioSnap in snapshot.children) {
                    for (moduloSnap in usuarioSnap.children) {
                        for (notaSnap in moduloSnap.children) {
                            val calificacion = notaSnap.getValue(Calificacion::class.java)
                            if (calificacion != null) {
                                lista.add(calificacion)
                            }
                        }
                    }
                }
                onResult(lista)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
