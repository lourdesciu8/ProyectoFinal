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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.data.FirebaseStorageHelper
import com.example.navegacion.databinding.FragmentTemarioprofesorBinding
import com.example.navegacion.ui.adapter.TemarioAdapter
import com.example.navegacion.ui.model.Temario
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class TemarioProfesorFragment : Fragment() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentTemarioprofesorBinding
    private lateinit var adapter: TemarioAdapter
    private lateinit var examenAdapter: TemarioAdapter
    private var moduloActual: String? = null
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()

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

        calendarioViewModel.obtenerModuloDelProfesorNombre { modulo ->
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

    private fun cargarTemariosDelProfesor(modulo: String) {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("temarios")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaFiltrada = mutableListOf<Temario>()
                for (temarioSnap in snapshot.children) {
                    val temario = temarioSnap.getValue(Temario::class.java)
                    if (temario != null && temario.uid == uidActual && temario.modulo == modulo) {
                        temario.id = temarioSnap.key
                        listaFiltrada.add(temario)
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
                    if (examen != null && examen.uid == uidActual && examen.modulo == modulo) {
                        examen.id = snap.key
                        listaFiltrada.add(examen)
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
