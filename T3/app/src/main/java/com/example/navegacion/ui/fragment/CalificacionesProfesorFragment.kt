/*package com.example.navegacion.ui.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentCalificacionesProfesorBinding
import com.example.navegacion.ui.adapter.CalificacionAdapter
import com.example.navegacion.ui.model.Calificacion
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

class CalificacionesProfesorFragment : Fragment() {

    private lateinit var binding: FragmentCalificacionesProfesorBinding
    private lateinit var adapter: CalificacionAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var alumnosMap: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalificacionesProfesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Inicializar referencia a Firebase Realtime Database
        database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")

        // Configuración del RecyclerView + animación
        val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_from_bottom)
        binding.recyclerCalificaciones.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCalificaciones.layoutAnimation = controller

        // Inicializar adapter con funciones lambda de editar y borrar
        adapter = CalificacionAdapter(mutableListOf(),
            onEditarClick = { calif -> mostrarDialogoEdicion(calif) },
            onBorrarClick = { calif -> eliminarCalificacion(calif) }
        )
        binding.recyclerCalificaciones.adapter = adapter

        // Botón de volver
        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Cargar módulos y alumnos al iniciar
        cargarModulos()
        cargarAlumnos()

        // Evento: al cambiar selección del spinner de alumnos o módulos → recargar calificaciones
        binding.spinnerAlumnos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                cargarCalificaciones()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spinnerModulos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                cargarCalificaciones()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Botón para añadir nueva calificación
        binding.btnAnadirCalificacion.setOnClickListener() {
            mostrarDialogoEdicion(null) // null = nueva calificación
        }
    }

    //Carga todos los módulos desde el nodo "modulos" de Firebase
    private fun cargarModulos() {
        val ref = database.getReference("modulos")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.child("nombre").getValue(String::class.java) }
                val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lista)
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerModulos.adapter = adapterSpinner
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //Carga todos los usuarios con rol "Alumno" desde Firebase
    private fun cargarAlumnos() {
        val ref = database.getReference("usuarios")
        ref.orderByChild("rol").equalTo("Alumno")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = mutableListOf<String>()
                    val mapa = mutableMapOf<String, String>()
                    for (snap in snapshot.children) {
                        val uid = snap.key ?: continue
                        val nombre = snap.child("nombre").getValue(String::class.java) ?: "Desconocido"
                        nombres.add(nombre)
                        mapa[uid] = nombre
                    }
                    alumnosMap = mapa
                    val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerAlumnos.adapter = adapterSpinner
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //Carga las calificaciones de un alumno para un módulo seleccionado
    private fun cargarCalificaciones() {
        val modulo = binding.spinnerModulos.selectedItem?.toString() ?: return
        val nombreAlumno = binding.spinnerAlumnos.selectedItem?.toString() ?: return
        val alumnoUID = alumnosMap.entries.find { it.value == nombreAlumno }?.key ?: return

        val ref = database.getReference("calificaciones").child(alumnoUID).child(modulo)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Calificacion>()
                for (snap in snapshot.children) {
                    val cal = snap.getValue(Calificacion::class.java)
                    cal?.let {
                        it.id = snap.key
                        lista.add(it)
                    }
                }
                adapter.actualizarLista(lista)
                binding.recyclerCalificaciones.scheduleLayoutAnimation()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //Muestra un AlertDialog para crear o editar una calificación
    private fun mostrarDialogoEdicion(calif: Calificacion?) {
        val editView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_nueva_calificacion, null)
        val edtTitulo = editView.findViewById<EditText>(R.id.edtTitulo)
        val edtNota = editView.findViewById<EditText>(R.id.edtNota)
        val edtFecha = editView.findViewById<EditText>(R.id.edtFecha) //Se muestra calendario
        edtFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val fechaFormateada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                edtFecha.setText(fechaFormateada)
            }, anio, mes, dia)

            datePicker.show()
        }

        val spTipo = editView.findViewById<Spinner>(R.id.spinnerTipo)

        val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("Examen", "Ejercicio"))
        spTipo.adapter = adapterTipo

        // Si estamos editando una calificación, rellenamos los campos
        if (calif != null) {
            edtTitulo.setText(calif.titulo)
            edtNota.setText(calif.nota.toString())
            edtFecha.setText(calif.fecha)
            spTipo.setSelection(if (calif.tipo == "Ejercicio") 1 else 0)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (calif == null) "Nueva calificación" else "Editar calificación")
            .setView(editView)
            .setPositiveButton("Guardar") { _, _ ->
                val nueva = Calificacion(
                    profesorUID = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    nombreProfesor = FirebaseAuth.getInstance().currentUser?.displayName ?: "Desconocido",
                    alumnoUID = alumnosMap.entries.find { it.value == binding.spinnerAlumnos.selectedItem.toString() }?.key ?: "",
                    nombreAlumno = binding.spinnerAlumnos.selectedItem.toString(),
                    titulo = edtTitulo.text.toString(),
                    tipo = spTipo.selectedItem.toString(),
                    nota = edtNota.text.toString().toDoubleOrNull() ?: 0.0,
                    fecha = edtFecha.text.toString(),
                    modulo = binding.spinnerModulos.selectedItem.toString()
                )


                val ref = database.getReference("calificaciones")
                    .child(nueva.alumnoUID)
                    .child(nueva.modulo)

                if (calif?.id != null) {
                    ref.child(calif.id!!).setValue(nueva)
                } else {
                    ref.push().setValue(nueva)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    //Elimina una calificación desde Firebase y muestra Snackbar
    private fun eliminarCalificacion(calif: Calificacion) {
        val ref = database.getReference("calificaciones")
            .child(calif.alumnoUID)
            .child(calif.modulo)
            .child(calif.id ?: return)

        ref.removeValue().addOnSuccessListener {
            Snackbar.make(binding.root, "Calificación eliminada", Snackbar.LENGTH_SHORT).show()
        }
    }
}*/
package com.example.navegacion.ui.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentCalificacionesProfesorBinding
import com.example.navegacion.ui.adapter.CalificacionAdapter
import com.example.navegacion.ui.model.Calificacion
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

class CalificacionesProfesorFragment : Fragment() {

    private lateinit var binding: FragmentCalificacionesProfesorBinding
    private lateinit var adapter: CalificacionAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var alumnosMap: Map<String, String>
    private var moduloProfesor: String? = null
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalificacionesProfesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")

        val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_from_bottom)
        binding.recyclerCalificaciones.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCalificaciones.layoutAnimation = controller

        adapter = CalificacionAdapter(mutableListOf(),
            onEditarClick = { calif -> mostrarDialogoEdicion(calif) },
            onBorrarClick = { calif -> eliminarCalificacion(calif) }
        )
        binding.recyclerCalificaciones.adapter = adapter

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        calendarioViewModel.obtenerModuloDelProfesorNombre { modulo ->
            moduloProfesor = modulo
            binding.txtModuloActual.text = "Módulo: $modulo"
            cargarAlumnos()
        }

        binding.spinnerAlumnos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                cargarCalificaciones()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.btnAnadirCalificacion.setOnClickListener {
            mostrarDialogoEdicion(null)
        }
    }

    private fun cargarAlumnos() {
        val ref = database.getReference("usuarios")
        ref.orderByChild("rol").equalTo("Alumno")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = mutableListOf<String>()
                    val mapa = mutableMapOf<String, String>()
                    for (snap in snapshot.children) {
                        val uid = snap.key ?: continue
                        val nombre = snap.child("nombre").getValue(String::class.java) ?: "Desconocido"
                        nombres.add(nombre)
                        mapa[uid] = nombre
                    }
                    alumnosMap = mapa
                    val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerAlumnos.adapter = adapterSpinner
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarCalificaciones() {
        val modulo = moduloProfesor ?: return
        val nombreAlumno = binding.spinnerAlumnos.selectedItem?.toString() ?: return
        val alumnoUID = alumnosMap.entries.find { it.value == nombreAlumno }?.key ?: return

        val ref = database.getReference("calificaciones").child(alumnoUID).child(modulo)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Calificacion>()
                for (snap in snapshot.children) {
                    val cal = snap.getValue(Calificacion::class.java)
                    cal?.let {
                        it.id = snap.key
                        lista.add(it)
                    }
                }
                adapter.actualizarLista(lista)
                binding.recyclerCalificaciones.scheduleLayoutAnimation()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarDialogoEdicion(calif: Calificacion?) {
        val editView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_nueva_calificacion, null)
        val edtTitulo = editView.findViewById<EditText>(R.id.edtTitulo)
        val edtNota = editView.findViewById<EditText>(R.id.edtNota)
        val edtFecha = editView.findViewById<EditText>(R.id.edtFecha)
        edtFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val fechaFormateada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                edtFecha.setText(fechaFormateada)
            }, anio, mes, dia)

            datePicker.show()
        }

        val spTipo = editView.findViewById<Spinner>(R.id.spinnerTipo)
        val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("Examen", "Ejercicio"))
        spTipo.adapter = adapterTipo

        if (calif != null) {
            edtTitulo.setText(calif.titulo)
            edtNota.setText(calif.nota.toString())
            edtFecha.setText(calif.fecha)
            spTipo.setSelection(if (calif.tipo == "Ejercicio") 1 else 0)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (calif == null) "Nueva calificación" else "Editar calificación")
            .setView(editView)
            .setPositiveButton("Guardar") { _, _ ->
                val nueva = Calificacion(
                    profesorUID = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    nombreProfesor = FirebaseAuth.getInstance().currentUser?.displayName ?: "Desconocido",
                    alumnoUID = alumnosMap.entries.find { it.value == binding.spinnerAlumnos.selectedItem.toString() }?.key ?: "",
                    nombreAlumno = binding.spinnerAlumnos.selectedItem.toString(),
                    titulo = edtTitulo.text.toString(),
                    tipo = spTipo.selectedItem.toString(),
                    nota = edtNota.text.toString().toDoubleOrNull() ?: 0.0,
                    fecha = edtFecha.text.toString(),
                    modulo = moduloProfesor ?: ""
                )

                val ref = database.getReference("calificaciones")
                    .child(nueva.alumnoUID)
                    .child(nueva.modulo)

                if (calif?.id != null) {
                    ref.child(calif.id!!).setValue(nueva)
                } else {
                    ref.push().setValue(nueva)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCalificacion(calif: Calificacion) {
        val ref = database.getReference("calificaciones")
            .child(calif.alumnoUID)
            .child(calif.modulo)
            .child(calif.id ?: return)

        ref.removeValue().addOnSuccessListener {
            Snackbar.make(binding.root, "Calificación eliminada", Snackbar.LENGTH_SHORT).show()
        }
    }
}
