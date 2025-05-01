package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.databinding.FragmentCalificacionesAlumnoBinding
import com.example.navegacion.ui.adapter.CalificacionAdapter
import com.example.navegacion.ui.model.Calificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CalificacionesAlumnoFragment : Fragment() {

    private lateinit var binding: FragmentCalificacionesAlumnoBinding
    private lateinit var adapter: CalificacionAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var alumnoUID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalificacionesAlumnoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
        alumnoUID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Configurar RecyclerView
        binding.recyclerNotasAlumno.layoutManager = LinearLayoutManager(requireContext())
        adapter = CalificacionAdapter(
            mutableListOf(),
            onEditarClick = {},   // función vacía para alumno
            onBorrarClick = {},   // función vacía para alumno
            esProfesor = false
        )
        binding.recyclerNotasAlumno.adapter = adapter

        // Cargar módulos disponibles para el alumno
        cargarModulosAlumno(alumnoUID)

        // Listener para el spinner
        binding.spinnerModulosAlumno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val moduloSeleccionado = binding.spinnerModulosAlumno.selectedItem?.toString() ?: return
                val alumnoUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
                cargarCalificacionesAlumnoPorModulo(alumnoUID, moduloSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun cargarModulosAlumno(alumnoUID: String) {
        val ref = database.getReference("modulos")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val modulos = snapshot.children.mapNotNull { it.child("nombre").getValue(String::class.java) }
                val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, modulos)
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerModulosAlumno.adapter = adapterSpinner

                // Lanzar la carga del primer módulo automáticamente si hay módulos
                if (modulos.isNotEmpty()) {
                    cargarCalificacionesAlumnoPorModulo(alumnoUID, modulos[0])
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun cargarCalificacionesAlumnoPorModulo(alumnoUID: String, modulo: String) {
        val ref = database.getReference("calificaciones").child(alumnoUID).child(modulo)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Calificacion>()
                for (notaSnap in snapshot.children) {
                    val calif = notaSnap.getValue(Calificacion::class.java)
                    calif?.let {
                        it.id = notaSnap.key
                        lista.add(it)
                    }
                }

                adapter.actualizarLista(lista)

                // Comprobamos si la lista de calificaciones está vacía
                if (lista.isEmpty()) {
                    // Mostramos el mensaje de "sin calificaciones"
                    binding.tvSinCalificaciones.visibility = View.VISIBLE

                    // Ocultamos el TextView de promedio porque no hay notas
                    binding.tvPromedio.visibility = View.GONE
                } else {
                    // Ocultamos el mensaje de "sin calificaciones" porque sí hay notas
                    binding.tvSinCalificaciones.visibility = View.GONE

                    // Calculamos la media de las notas
                    val media = lista.map { it.nota }.average()
                    val redondeada = String.format("%.2f", media)
                    binding.tvPromedio.text = "Media del módulo: $redondeada"
                    binding.tvPromedio.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

}
