package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.model.Temario
import com.example.navegacion.ui.adapter.TemarioAdapter
import com.google.firebase.database.*
class TemarioAlumnoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TemarioAdapter
    private lateinit var listaTemarios: MutableList<Temario>
    private lateinit var dbRef: DatabaseReference
    private lateinit var spinnerModulos: Spinner
    private lateinit var recyclerExamenes: RecyclerView
    private lateinit var examenAdapter: TemarioAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_temarioalumno, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerExamenes = view.findViewById(R.id.recyclerExamenes)
        recyclerExamenes.layoutManager = LinearLayoutManager(requireContext())
        examenAdapter = TemarioAdapter(mutableListOf())
        recyclerExamenes.adapter = examenAdapter

        spinnerModulos = view.findViewById(R.id.spinnerModulos)

        // Cargar los nombres de los módulos desde la bbdd de Firebase
        val modulosRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("modulos")

        modulosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaModulos = mutableListOf<String>()
                for (moduloSnap in snapshot.children) {
                    val nombreModulo = moduloSnap.child("nombre").getValue(String::class.java)
                    nombreModulo?.let { listaModulos.add(it) }
                }

                val adaptadorSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaModulos)
                adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerModulos.adapter = adaptadorSpinner
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar módulos", Toast.LENGTH_SHORT).show()
            }
        })

        // Evento al seleccionar módulo
        spinnerModulos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val moduloSeleccionado = parent.getItemAtPosition(position).toString()
                cargarTemariosPorModulo(moduloSeleccionado)
                cargarExamenesPorModulo(moduloSeleccionado)
            }

            // Filtrado de temarios por módulo seleccionado
            private fun cargarTemariosPorModulo(modulo: String) {
                val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("temarios")

                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val listaTemarios = mutableListOf<Temario>()
                        for (temarioSnap in snapshot.children) {
                            val temario = temarioSnap.getValue(Temario::class.java)
                            if (temario != null && temario.modulo == modulo) {
                                listaTemarios.add(temario)
                            }
                        }
                        adapter.actualizarLista(listaTemarios)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Error al cargar temarios", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Botón para volver
        view.findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerTemarios)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        listaTemarios = mutableListOf()
        adapter = TemarioAdapter(listaTemarios)
        recyclerView.adapter = adapter
    }

    private fun cargarExamenesPorModulo(modulo: String) {
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("examenes")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaExamenes = mutableListOf<Temario>()
                for (snap in snapshot.children) {
                    val examen = snap.getValue(Temario::class.java)
                    if (examen != null && examen.modulo == modulo) {
                        listaExamenes.add(examen)
                    }
                }
                examenAdapter.actualizarLista(listaExamenes)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar exámenes", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
