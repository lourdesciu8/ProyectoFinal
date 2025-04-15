package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.model.Temario
import com.example.navegacion.ui.adapter.TemarioAdapter
import com.google.firebase.database.*

class TemarioAlumnoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TemarioAdapter
    private lateinit var listaTemarios: MutableList<Temario>
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_temarioalumno, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón para volver
        view.findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerTemarios)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        listaTemarios = mutableListOf()
        adapter = TemarioAdapter(listaTemarios)
        recyclerView.adapter = adapter

        // Leer datos de Realtime Database
        //dbRef = FirebaseDatabase.getInstance().getReference("temarios")
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app").getReference("temarios")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaTemarios.clear()
                for (temarioSnap in snapshot.children) {
                    val temario = temarioSnap.getValue(Temario::class.java)
                    temario?.let { listaTemarios.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}
