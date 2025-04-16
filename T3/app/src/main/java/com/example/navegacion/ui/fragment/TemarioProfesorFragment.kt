package com.example.navegacion.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.data.FirebaseStorageHelper
import com.example.navegacion.ui.adapter.TemarioAdapter
import com.example.navegacion.ui.model.Temario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class TemarioProfesorFragment : Fragment() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var adapter: TemarioAdapter
    private lateinit var spinnerModulos: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_temarioprofesor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Spinner
        spinnerModulos = view.findViewById(R.id.spinnerModulos)
        cargarModulosDesdeFirebase()

        // Botón volver
        view.findViewById<ImageButton>(R.id.btnVolver)?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerTemariosProfesor)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TemarioAdapter(
            mutableListOf(),
            esProfesor = true,
            onEliminarClick = { temario -> eliminarTemario(temario) }
        )

        recyclerView.adapter = adapter

        // Botón subir archivo
        val botonSeleccionar = view.findViewById<Button>(R.id.btnSeleccionarArchivo)
        botonSeleccionar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            filePickerLauncher.launch(intent)
        }

        // File picker launcher: permite al usuario seleccionar un archivo PDF desde su dispositivo
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    val user = FirebaseAuth.getInstance().currentUser
                    val moduloSeleccionado = spinnerModulos.selectedItem.toString()

                    // Mostrar un AlertDialog para ingresar nombre archivo personalizado
                    val editText = EditText(requireContext()).apply {
                        hint = "Nombre del archivo"
                        setText("temario_${System.currentTimeMillis()}")
                    }

                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Nombre del archivo")
                        .setView(editText)
                        .setPositiveButton("Aceptar") { _, _ ->
                            val nombreArchivo = editText.text.toString().trim().ifEmpty {
                                "temario_${System.currentTimeMillis()}.pdf"
                            } + ".pdf"

                            FirebaseStorageHelper.subirArchivo(
                                requireContext(),
                                it,
                                nombreArchivo,
                                moduloSeleccionado,
                                onSuccess = { url ->
                                    FirebaseStorageHelper.guardarEnRealtimeDatabase(requireContext(), nombreArchivo, url, moduloSeleccionado)
                                    Toast.makeText(requireContext(), "Archivo subido", Toast.LENGTH_LONG).show()
                                    cargarTemariosDelProfesor()
                                },
                                onError = {
                                    Toast.makeText(requireContext(), "Error al subir archivo", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        .setNegativeButton("Cancelar", null)
                        .create()

                    dialog.show()

                    dialog.window?.setLayout(
                        (resources.displayMetrics.widthPixels * 0.80).toInt(),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        }

        // Cargar temarios
        spinnerModulos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                cargarTemariosDelProfesor()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun cargarModulosDesdeFirebase() {
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("modulos")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaNombres = mutableListOf<String>()
                for (moduloSnap in snapshot.children) {
                    val nombre = moduloSnap.child("nombre").getValue(String::class.java)  //Se cargan asignaturas(modulos) desde la bbdd
                    nombre?.let { listaNombres.add(it) }
                }

                val adaptador = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaNombres)
                adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerModulos.adapter = adaptador
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar módulos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarTemariosDelProfesor() {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid
        val moduloSeleccionado = spinnerModulos.selectedItem.toString()

        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("temarios")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaFiltrada = mutableListOf<Temario>()
                for (temarioSnap in snapshot.children) {
                    val temario = temarioSnap.getValue(Temario::class.java)
                    temario?.let {
                        if (it.uid == uidActual && it.modulo == moduloSeleccionado) {
                            it.id = temarioSnap.key
                            listaFiltrada.add(it)
                        }
                    }
                }
                adapter.actualizarLista(listaFiltrada)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar temarios", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun eliminarTemario(temario: Temario) {
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("temarios")

        val temarioId = temario.id ?: return

        dbRef.child(temarioId).removeValue()
            .addOnSuccessListener {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(temario.url!!)
                storageRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Temario eliminado", Toast.LENGTH_SHORT).show()
                        cargarTemariosDelProfesor()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al eliminar del Storage", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }
}
