// src/main/java/com/example/navegacion/ui/fragment/HilosFragment.kt
package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.databinding.FragmentHilosBinding
import com.example.navegacion.ui.adapter.HilosAdapter
import com.example.navegacion.ui.model.Hilo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.core.os.bundleOf
import com.example.navegacion.R

class HilosFragment : Fragment() {

    private var _binding: FragmentHilosBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var usuariosRef: DatabaseReference
    private val userMap = mutableMapOf<String, String>() // uid → nombre

    private val listaHilos = mutableListOf<Hilo>()
    private lateinit var adapter: HilosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHilosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón volver
        binding.btnVolver.setOnClickListener { findNavController().navigateUp() }

        // Título
        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.title = "FORO-COMUN"

        // Referencias Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("hilos")
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios")

        // Carga mapa UID→nombre
        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (u in snapshot.children) {
                    val uid = u.key ?: continue
                    val nombre = u.child("nombre").getValue(String::class.java) ?: uid
                    userMap[uid] = nombre
                }
                setupList()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),
                    "Error cargando usuarios: ${error.message}", Toast.LENGTH_LONG).show()
                setupList()
            }
        })
    }

    private fun setupList() {
        // Adapter con el mapa de nombres
        adapter = HilosAdapter(listaHilos, userMap) { hilo ->
            findNavController().navigate(
                R.id.action_hilosFragment_to_detalleHiloFragment,
                bundleOf("idHilo" to hilo.idHilo)
            )
        }

        binding.rvHilos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HilosFragment.adapter
        }

        // Crear nuevo hilo
        binding.btnCrearHilo.setOnClickListener { showCrearHiloDialog() }

        // Carga los hilos existentes
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaHilos.clear()
                for (snap in snapshot.children) {
                    snap.getValue(Hilo::class.java)?.let {
                        listaHilos.add(it.copy(idHilo = snap.key ?: ""))
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),
                    "Error al leer hilos: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showCrearHiloDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Título del hilo"
            setPadding(24, 24, 24, 24)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo Hilo")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val titulo = input.text.toString().trim()
                if (titulo.isNotEmpty()) crearOAbrirHilo(titulo)
                else Toast.makeText(requireContext(),
                    "Debes escribir un título", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearOAbrirHilo(titulo: String) {
        // Si ya existe, abre
        listaHilos.find { it.titulo.equals(titulo, true) }?.let {
            findNavController().navigate(
                R.id.action_hilosFragment_to_detalleHiloFragment,
                bundleOf("idHilo" to it.idHilo)
            )
            return
        }
        // Si no existe, crea
        val newRef = dbRef.push()
        val idNuevo = newRef.key ?: return
        val autorUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val hiloNuevo = Hilo(
            idHilo       = idNuevo,
            titulo       = titulo,
            creadoPor    = autorUid,
            marcaTemporal    = System.currentTimeMillis()
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
                    "Error al crear hilo: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
