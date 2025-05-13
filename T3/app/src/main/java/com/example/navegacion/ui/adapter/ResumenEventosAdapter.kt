/*package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.model.Evento
import java.text.SimpleDateFormat
import java.util.*


class ResumenEventosAdapter(
    private val eventos: MutableList<Evento>,
    private val onClick: (Evento) -> Unit
) : RecyclerView.Adapter<ResumenEventosAdapter.EventoViewHolder>() {

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.txtTituloEvento)
        val fecha: TextView = view.findViewById(R.id.txtFechaEvento)
        val icono: ImageView = view.findViewById(R.id.iconoEvento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resumen_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.titulo.text = evento.titulo

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.fecha.text = evento.timestamp?.let { sdf.format(Date(it)) } ?: ""

        val iconoRes = when (evento.tipo.lowercase()) {
            "examen" -> R.drawable.ic_exam
            "tarea" -> R.drawable.ic_task
            else -> R.drawable.ic_event
        }
        holder.icono.setImageResource(iconoRes)

        holder.itemView.setOnClickListener { onClick(evento) }
    }

    override fun getItemCount() = eventos.size

    // 🔥 NUEVO para actualizar la lista
    fun updateEventos(nuevosEventos: List<Evento>) {
        eventos.clear()
        eventos.addAll(nuevosEventos)
        notifyDataSetChanged()
    }
}*/
package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.model.Evento
import java.text.SimpleDateFormat
import java.util.*


class ResumenEventosAdapter(
    private val eventos: MutableList<Evento>,
    private val onClick: (Evento) -> Unit
) : RecyclerView.Adapter<ResumenEventosAdapter.EventoViewHolder>() {

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.txtTituloEvento)
        val fecha: TextView = view.findViewById(R.id.txtFechaEvento)
        val icono: ImageView = view.findViewById(R.id.iconoEvento)
        val origen: TextView = view.findViewById(R.id.txtOrigenEvento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resumen_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.titulo.text = evento.titulo

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.fecha.text = evento.fecha?.let { sdf.format(Date(it)) } ?: ""

        val iconoRes = when (evento.tipo.lowercase()) {
            "examen" -> R.drawable.ic_exam
            "tarea" -> R.drawable.ic_task
            else -> R.drawable.ic_event
        }
        holder.icono.setImageResource(iconoRes)

        holder.origen.text = if (evento.esPersonal) "(Personal)" else "(Asignado)"

        holder.itemView.setOnClickListener { onClick(evento) }
    }

    override fun getItemCount() = eventos.size

    fun updateEventos(nuevosEventos: List<Evento>) {
        eventos.clear()
        eventos.addAll(nuevosEventos)
        notifyDataSetChanged()
    }
}


