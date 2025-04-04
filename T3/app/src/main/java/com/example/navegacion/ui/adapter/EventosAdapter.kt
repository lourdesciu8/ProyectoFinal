package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class EventosAdapter : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    private var eventosList: List<String> = emptyList()

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventoText: TextView = view.findViewById(R.id.eventoText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.eventoText.text = eventosList[position]
    }

    override fun getItemCount(): Int = eventosList.size

    // Método para actualizar la lista de eventos cuando se agregan nuevos
    fun updateEventos(eventos: List<String>) {
        eventosList = eventos
        notifyDataSetChanged()
    }
}
