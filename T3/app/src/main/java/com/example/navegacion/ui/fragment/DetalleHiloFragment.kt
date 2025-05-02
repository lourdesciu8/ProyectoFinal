// src/main/java/com/example/navegacion/ui/fragment/DetalleHiloFragment.kt
package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.databinding.FragmentDetalleHiloBinding
import com.example.navegacion.ui.adapter.MensajesAdapter
import com.example.navegacion.ui.model.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetalleHiloFragment : Fragment() {

    private var _binding: FragmentDetalleHiloBinding? = null
    private val binding get() = _binding!!

    private lateinit var msgDbRef: DatabaseReference
    private lateinit var usuariosRef: DatabaseReference
    private val userMap = mutableMapOf<String, String>()

    private val listaMensajes = mutableListOf<Mensaje>()
    private lateinit var adapter: MensajesAdapter

    private var idHilo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idHilo = arguments?.getString("idHilo") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleHiloBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón volver
        binding.btnVolver.setOnClickListener { findNavController().navigateUp() }

        // Título
        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.title = "Hilo: $idHilo"

        // Referencias Firebase
        msgDbRef = FirebaseDatabase.getInstance()
            .getReference("hilos")
            .child(idHilo)
            .child("mensajes")
        usuariosRef = FirebaseDatabase.getInstance()
            .getReference("usuarios")

        // Carga mapa UID→nombre
        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnap in snapshot.children) {
                    val uid = userSnap.key ?: continue
                    val nombre = userSnap.child("nombre")
                        .getValue(String::class.java)
                        ?: uid
                    userMap[uid] = nombre
                }
                setupRecyclerView()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),
                    "Error cargando usuarios: ${error.message}",
                    Toast.LENGTH_LONG).show()
                setupRecyclerView()
            }
        })
    }

    private fun setupRecyclerView() {
        // LayoutManager con scroll al final
        val lm = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.rvMensajes.layoutManager = lm

        // Adapter con el mapa de nombres
        adapter = MensajesAdapter(listaMensajes, userMap)
        binding.rvMensajes.adapter = adapter

        // Escucha mensajes en tiempo real
        msgDbRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Mensaje::class.java)?.let { m ->
                    listaMensajes.add(m.copy(idMensaje = snapshot.key ?: ""))
                    adapter.notifyItemInserted(listaMensajes.size - 1)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onChildRemoved(snapshot: DataSnapshot) = Unit
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),
                    "Error al leer mensajes: ${error.message}",
                    Toast.LENGTH_LONG).show()
            }
        })

        // Envío de mensaje
        binding.btnEnviarMensaje.setOnClickListener {
            val texto = binding.etNuevoMensaje.text.toString().trim()
            if (texto.isNotEmpty()) {
                val newMsgRef = msgDbRef.push()
                val msg = Mensaje(
                    idMensaje     = newMsgRef.key ?: "",
                    texto         = texto,
                    autor         = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    marcaTemporal = System.currentTimeMillis()
                )
                newMsgRef.setValue(msg)
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(),
                            "Error enviando mensaje: ${e.message}",
                            Toast.LENGTH_LONG).show()
                    }
                binding.etNuevoMensaje.text?.clear()
            } else {
                Toast.makeText(requireContext(),
                    "Escribe algo primero",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
