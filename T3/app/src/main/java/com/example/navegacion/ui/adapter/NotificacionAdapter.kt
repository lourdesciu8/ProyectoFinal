package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.model.Notificacion

class NotificacionAdapter(
    private val notificaciones: List<Pair<String, Notificacion>>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<NotificacionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.txtTitulo)
        val cuerpo: TextView = itemView.findViewById(R.id.txtCuerpo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = notificaciones.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, notificacion) = notificaciones[position]
        holder.titulo.text = notificacion.titulo
        holder.cuerpo.text = notificacion.cuerpo
        holder.itemView.setOnClickListener { onItemClick(id) }
    }
}
