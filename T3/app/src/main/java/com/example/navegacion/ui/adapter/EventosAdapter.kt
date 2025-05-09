package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.model.Evento

class EventosAdapter(
    private var eventosList: List<Evento> = listOf(),
    private val onEditar: (Evento) -> Unit,
    private val onEliminar: (Evento) -> Unit
) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tituloText: TextView = view.findViewById(R.id.txtTitulo)
        val descripcionText: TextView = view.findViewById(R.id.txtDescripcion)
        val tipoIcono: ImageView = view.findViewById(R.id.iconoTipo)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventosList[position]

        holder.tituloText.text = evento.titulo
        holder.descripcionText.text = evento.descripcion

        // Cambiar icono o color según tipo
        when (evento.tipo) {
            "Examen" -> holder.tipoIcono.setImageResource(R.drawable.ic_exam)
            "Tarea" -> holder.tipoIcono.setImageResource(R.drawable.ic_task)
            else -> holder.tipoIcono.setImageResource(R.drawable.ic_event)
        }

        holder.btnEditar.setOnClickListener { onEditar(evento) }
        holder.btnEliminar.setOnClickListener { onEliminar(evento) }
    }

    override fun getItemCount(): Int = eventosList.size

    fun updateEventos(eventos: List<Evento>) {
        this.eventosList = eventos
        notifyDataSetChanged()
    }
}