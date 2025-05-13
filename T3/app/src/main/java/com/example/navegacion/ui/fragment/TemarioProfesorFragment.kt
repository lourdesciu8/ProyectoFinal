/*package com.example.navegacion.ui.fragment

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
import android.widget.RadioGroup
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
import com.example.navegacion.databinding.FragmentTemarioprofesorBinding
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
    private lateinit var binding: FragmentTemarioprofesorBinding
    private lateinit var adapter: TemarioAdapter
    private lateinit var spinnerModulos: Spinner
    private lateinit var recyclerExamenes: RecyclerView
    private lateinit var examenAdapter: TemarioAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTemarioprofesorBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //configurarSpinnerModulos()

        // Spinner
        spinnerModulos = view.findViewById(R.id.spinnerModulos)
        cargarModulosDesdeFirebase()

        // Botón volver
        view.findViewById<ImageButton>(R.id.btnVolver)?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // RecyclerView para temarios
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerTemariosProfesor)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TemarioAdapter(
            mutableListOf(),
            esProfesor = true,
            onEliminarClick = { temario -> eliminarTemario(temario) }
        )

        recyclerView.adapter = adapter

        // RecyclerView para exámenes
        recyclerExamenes = view.findViewById(R.id.recyclerExamenesProfesor)
        recyclerExamenes.layoutManager = LinearLayoutManager(requireContext())

        examenAdapter = TemarioAdapter(
            mutableListOf(),
            esProfesor = true,
            onEliminarClick = { temario -> eliminarExamen(temario) }
        )
        recyclerExamenes.adapter = examenAdapter


        // Botón subir archivo
        val btnSeleccionarArchivo = view.findViewById<Button>(R.id.btnSeleccionarArchivo)
        val radioTipoArchivo = view.findViewById<RadioGroup>(R.id.radioTipoArchivo)

        btnSeleccionarArchivo.setOnClickListener {
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

                    // Obtener tipo de archivo (temario o examen)
                    val tipoNodo = when (requireView().findViewById<RadioGroup>(R.id.radioTipoArchivo).checkedRadioButtonId) {
                        R.id.radioTemario -> "temarios"
                        R.id.radioExamen -> "examenes"
                        else -> "temarios"
                    }

                    // Mostrar diálogo para nombre personalizado
                    val editText = EditText(requireContext()).apply {
                        hint = "Nombre del archivo"
                        setText("${tipoNodo.dropLast(1)}_${System.currentTimeMillis()}") // "temario_" o "examen_"
                    }

                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Nombre del archivo")
                        .setView(editText)
                        .setPositiveButton("Aceptar") { _, _ ->
                            val nombreArchivo = editText.text.toString().trim().ifEmpty {
                                "${tipoNodo.dropLast(1)}_${System.currentTimeMillis()}"
                            } + ".pdf"

                            FirebaseStorageHelper.subirArchivo(
                                context = requireContext(),
                                uri = it,
                                nombreArchivo = nombreArchivo,
                                modulo = moduloSeleccionado,
                                carpeta = tipoNodo, // esto será "temarios" o "examenes"
                                onSuccess = { url ->
                                    FirebaseStorageHelper.guardarEnRealtimeDatabase(
                                        context = requireContext(),
                                        nombreArchivo = nombreArchivo,
                                        url = url,
                                        modulo = moduloSeleccionado,
                                        nodo = tipoNodo
                                    )
                                    Toast.makeText(requireContext(), "Archivo subido", Toast.LENGTH_LONG).show()

                                    // Recargar el tipo de lista correspondiente
                                    if (tipoNodo == "temarios") {
                                        cargarTemariosDelProfesor(moduloSeleccionado)
                                    } else {
                                        cargarExamenesDelProfesor(moduloSeleccionado)
                                    }
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
                val moduloSeleccionado = parent.getItemAtPosition(position).toString()
                cargarTemariosDelProfesor(moduloSeleccionado)
                cargarExamenesDelProfesor(moduloSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun eliminarExamen(examen: Temario) {
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("examenes")

        val examenId = examen.id ?: return

        dbRef.child(examenId).removeValue()
            .addOnSuccessListener {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(examen.url!!)
                storageRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Examen eliminado", Toast.LENGTH_SHORT).show()
                        val moduloSeleccionado = binding.spinnerModulos.selectedItem.toString()
                        cargarExamenesDelProfesor(moduloSeleccionado)

                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar examen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarExamenesDelProfesor(modulo: String) {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid

        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("examenes") // Nodo para exámenes

        //ValueEventListener escucha cambios en la base de datos automaticamente
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaFiltrada = mutableListOf<Temario>()
                for (snap in snapshot.children) {
                    val examen = snap.getValue(Temario::class.java)
                    examen?.let {
                        if (it.uid == uidActual && it.modulo == modulo) {
                            it.id = snap.key
                            listaFiltrada.add(it)
                        }
                    }
                }
                examenAdapter.actualizarLista(listaFiltrada)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar exámenes", Toast.LENGTH_SHORT).show()
            }
        })
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

                // Cargar automáticamente el contenido del primer módulo al abrir el fragment
                if (listaNombres.isNotEmpty()) {
                    val moduloSeleccionado = listaNombres[0]
                    cargarTemariosDelProfesor(moduloSeleccionado)
                    cargarExamenesDelProfesor(moduloSeleccionado)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar módulos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarTemariosDelProfesor(modulo: String) {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid

        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("temarios")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaFiltrada = mutableListOf<Temario>()
                for (temarioSnap in snapshot.children) {
                    val temario = temarioSnap.getValue(Temario::class.java)
                    temario?.let {
                        if (it.uid == uidActual && it.modulo == modulo) {
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
                        val moduloSeleccionado = binding.spinnerModulos.selectedItem.toString()
                        cargarTemariosDelProfesor(moduloSeleccionado)
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
*/
package com.example.navegacion.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.data.FirebaseStorageHelper
import com.example.navegacion.databinding.FragmentTemarioprofesorBinding
import com.example.navegacion.ui.adapter.TemarioAdapter
import com.example.navegacion.ui.model.Temario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class TemarioProfesorFragment : Fragment() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentTemarioprofesorBinding
    private lateinit var adapter: TemarioAdapter
    private lateinit var examenAdapter: TemarioAdapter
    private var moduloActual: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTemarioprofesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    seleccionarArchivoYSubir(it)
                }
            }
        }

        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        adapter = TemarioAdapter(mutableListOf(), esProfesor = true) { eliminarTemario(it) }
        binding.recyclerTemariosProfesor.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTemariosProfesor.adapter = adapter

        examenAdapter = TemarioAdapter(mutableListOf(), esProfesor = true) { eliminarExamen(it) }
        binding.recyclerExamenesProfesor.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExamenesProfesor.adapter = examenAdapter

        obtenerModuloDelProfesor { modulo ->
            modulo?.let {
                moduloActual = it
                cargarTemariosDelProfesor(it)
                cargarExamenesDelProfesor(it)
            }
        }

        binding.btnSeleccionarArchivo.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            filePickerLauncher.launch(intent)
        }
    }

    private fun seleccionarArchivoYSubir(uri: Uri) {
        val modulo = moduloActual ?: return
        val tipoNodo = when (binding.radioTipoArchivo.checkedRadioButtonId) {
            R.id.radioTemario -> "temarios"
            R.id.radioExamen -> "examenes"
            else -> "temarios"
        }

        val input = EditText(requireContext()).apply {
            hint = "Nombre del archivo"
            setText("${tipoNodo.dropLast(1)}_${System.currentTimeMillis()}.pdf")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nombre del archivo")
            .setView(input)
            .setPositiveButton("Aceptar") { _, _ ->
                val nombre = input.text.toString().trim().ifEmpty {
                    "${tipoNodo.dropLast(1)}_${System.currentTimeMillis()}.pdf"
                }

                FirebaseStorageHelper.subirArchivo(
                    context = requireContext(),
                    uri = uri,
                    nombreArchivo = nombre,
                    modulo = modulo,
                    carpeta = tipoNodo,
                    onSuccess = { url ->
                        FirebaseStorageHelper.guardarEnRealtimeDatabase(
                            context = requireContext(),
                            nombreArchivo = nombre,
                            url = url,
                            modulo = modulo,
                            nodo = tipoNodo
                        )
                        Toast.makeText(requireContext(), "Archivo subido", Toast.LENGTH_SHORT).show()
                        if (tipoNodo == "temarios") cargarTemariosDelProfesor(modulo) else cargarExamenesDelProfesor(modulo)
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Error al subir archivo", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun obtenerModuloDelProfesor(callback: (String?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(null)
        val ref = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("modulos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (moduloSnap in snapshot.children) {
                    if (moduloSnap.child("Profesores").hasChild(uid)) {
                        callback(moduloSnap.key)
                        return
                    }
                }
                callback(null)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    private fun cargarTemariosDelProfesor(modulo: String) {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("temarios")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaFiltrada = mutableListOf<Temario>()
                for (temarioSnap in snapshot.children) {
                    val temario = temarioSnap.getValue(Temario::class.java)
                    if (temario != null) {
                        if (temario?.uid == uidActual && temario.modulo == modulo) {
                            if (temario != null) {
                                temario.id = temarioSnap.key
                            }
                            if (temario != null) {
                                listaFiltrada.add(temario)
                            }
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

    private fun cargarExamenesDelProfesor(modulo: String) {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("examenes")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaFiltrada = mutableListOf<Temario>()
                for (snap in snapshot.children) {
                    val examen = snap.getValue(Temario::class.java)
                    if (examen != null) {
                        if (examen?.uid == uidActual && examen.modulo == modulo) {
                            if (examen != null) {
                                examen.id = snap.key
                            }
                            if (examen != null) {
                                listaFiltrada.add(examen)
                            }
                        }
                    }
                }
                examenAdapter.actualizarLista(listaFiltrada)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar exámenes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun eliminarTemario(temario: Temario) {
        val ref = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("temarios")
        val id = temario.id ?: return

        ref.child(id).removeValue().addOnSuccessListener {
            FirebaseStorage.getInstance().getReferenceFromUrl(temario.url!!).delete()
            Toast.makeText(requireContext(), "Temario eliminado", Toast.LENGTH_SHORT).show()
            moduloActual?.let { cargarTemariosDelProfesor(it) }
        }
    }

    private fun eliminarExamen(examen: Temario) {
        val ref = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("examenes")
        val id = examen.id ?: return

        ref.child(id).removeValue().addOnSuccessListener {
            FirebaseStorage.getInstance().getReferenceFromUrl(examen.url!!).delete()
            Toast.makeText(requireContext(), "Examen eliminado", Toast.LENGTH_SHORT).show()
            moduloActual?.let { cargarExamenesDelProfesor(it) }
        }
    }
}
