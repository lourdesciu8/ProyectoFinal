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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetalleHiloFragment : Fragment() {

    private var _binding: FragmentDetalleHiloBinding? = null
    private val binding get() = _binding!!

    private lateinit var msgDbRef: DatabaseReference
    private val listaMensajes = mutableListOf<Mensaje>()
    private lateinit var adapter: MensajesAdapter

    private var idHilo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idHilo = arguments?.getString("idHilo") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleHiloBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Botón Volver
        binding.btnVolver.setOnClickListener {
            findNavController().navigateUp()
        }

        // Ajusta el título de la ActionBar
        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.title = "Hilo: $idHilo"

        // Referencia a /hilos/{idHilo}/mensajes
        msgDbRef = FirebaseDatabase
            .getInstance()
            .getReference("hilos")
            .child(idHilo)
            .child("mensajes")

        // Configura RecyclerView y adapter
        adapter = MensajesAdapter(listaMensajes)
        binding.rvMensajes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DetalleHiloFragment.adapter
        }

        // Carga mensajes en tiempo real
        cargarMensajes()

        // Enviar nuevo mensaje
        binding.btnEnviarMensaje.setOnClickListener {
            val texto = binding.etNuevoMensaje.text.toString().trim()
            if (texto.isNotEmpty()) {
                enviarMensaje(texto)
                binding.etNuevoMensaje.text?.clear()
            } else {
                Toast.makeText(requireContext(),
                    "Escribe algo primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarMensajes() {
        listaMensajes.clear()
        msgDbRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val m = snapshot.getValue(Mensaje::class.java)
                if (m != null) {
                    listaMensajes.add(m.copy(idMensaje = snapshot.key ?: ""))
                    adapter.notifyItemInserted(listaMensajes.size - 1)
                    binding.rvMensajes.scrollToPosition(listaMensajes.size - 1)
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
    }

    private fun enviarMensaje(texto: String) {
        val newMsgRef = msgDbRef.push()
        val idMsg = newMsgRef.key ?: return
        val marca = System.currentTimeMillis()
        val autor = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val msg = Mensaje(
            idMensaje = idMsg,
            texto = texto,
            autor = autor,
            marcaTemporal = marca
        )

        newMsgRef.setValue(msg)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(),
                    "Error enviando mensaje: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
    }


}
