package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.os.bundleOf
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHilosBinding
import com.example.navegacion.ui.adapter.HilosAdapter
import com.example.navegacion.ui.model.Hilo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class HilosFragment : Fragment() {

    private var _binding: FragmentHilosBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private val listaHilos = mutableListOf<Hilo>()
    private lateinit var adapter: HilosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHilosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Botón Volver
        binding.btnVolver.setOnClickListener {
            findNavController().navigateUp()
        }
        // Título en la barra
        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.title = "FORO-COMUN"

        // Referencia a /hilos en RTDB
        dbRef = FirebaseDatabase.getInstance().getReference("hilos")

        // Adapter y RecyclerView
        adapter = HilosAdapter(listaHilos) { hilo ->
            // Navegar al detalle, pasando el id
            findNavController().navigate(
                R.id.action_hilosFragment_to_detalleHiloFragment,
                bundleOf("idHilo" to hilo.idHilo)
            )
        }
        binding.rvHilos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HilosFragment.adapter
        }

        // Botón crear hilo
        binding.btnCrearHilo.setOnClickListener {
            showCrearHiloDialog()
        }

        // Carga inicial de hilos
        cargarHilos()
    }

    private fun cargarHilos() {
        listaHilos.clear()
        dbRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                for (hiloSnap in snapshot.children) {
                    val h = hiloSnap.getValue(Hilo::class.java)
                    if (h != null) {
                        listaHilos.add(h.copy(idHilo = hiloSnap.key ?: ""))
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(requireContext(),
                    "Error al leer hilos: ${error.message}",
                    Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showCrearHiloDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Título del hilo"
            setPadding(24,24,24,24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo Hilo")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val titulo = input.text.toString().trim()
                if (titulo.isNotEmpty()) {
                    crearOAbrirHilo(titulo)
                } else {
                    Toast.makeText(requireContext(),
                        "Debes escribir un título", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearOAbrirHilo(titulo: String) {
        // Si ya existe, abrimos
        val existente = listaHilos.find { it.titulo.equals(titulo, true) }
        if (existente != null) {
            findNavController().navigate(
                R.id.action_hilosFragment_to_detalleHiloFragment,
                bundleOf("idHilo" to existente.idHilo)
            )
            return
        }

        // Si no existe, creamos
        val newRef = dbRef.push()
        val idNuevo = newRef.key ?: return
        val marca = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val autor = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val hiloNuevo = Hilo(
            idHilo = idNuevo,
            titulo = titulo,
            creadoPor = autor,
            marcaTemporal = marca
        )

        newRef.setValue(hiloNuevo)
            .addOnSuccessListener {
                listaHilos.add(hiloNuevo)
                adapter.notifyItemInserted(listaHilos.size - 1)
                findNavController().navigate(
                    R.id.action_hilosFragment_to_detalleHiloFragment,
                    bundleOf("idHilo" to hiloNuevo.idHilo)
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(),
                    "Error al crear hilo: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
