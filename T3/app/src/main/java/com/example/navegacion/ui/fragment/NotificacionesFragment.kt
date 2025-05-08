package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.adapter.NotificacionAdapter
import com.example.navegacion.ui.model.Notificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificacionesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificacionAdapter
    private lateinit var uid: String
    private val notificaciones = mutableListOf<Pair<String, Notificacion>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notificaciones, container, false)
        recyclerView = view.findViewById(R.id.recyclerNotificaciones)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        uid = FirebaseAuth.getInstance().currentUser?.uid ?: return view

        adapter = NotificacionAdapter(notificaciones) { id ->
            eliminarNotificacion(id)
        }
        recyclerView.adapter = adapter

        val ref = FirebaseDatabase
            .getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("notificaciones").child(uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificaciones.clear()
                for (child in snapshot.children) {
                    val notif = child.getValue(Notificacion::class.java)
                    if (notif != null) {
                        notificaciones.add(Pair(child.key!!, notif))
                    }
                }
                adapter.notifyDataSetChanged()

                // Eliminar todas las notificaciones luego de mostrarlas
                ref.removeValue()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error si quieres
            }
        })

        return view
    }

    private fun eliminarNotificacion(id: String) {
        val ref = FirebaseDatabase
            .getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("notificaciones").child(uid).child(id)
        ref.removeValue()
    }
}
