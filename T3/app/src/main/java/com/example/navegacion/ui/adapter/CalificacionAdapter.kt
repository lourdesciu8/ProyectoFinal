package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.model.Calificacion

class CalificacionAdapter(
    private var lista: MutableList<Calificacion>,
    private val onEditarClick: (Calificacion) -> Unit = {},
    private val onBorrarClick: (Calificacion) -> Unit = {},
    private val esProfesor: Boolean = true
) : RecyclerView.Adapter<CalificacionAdapter.CalificacionViewHolder>() {

    class CalificacionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val tvNota: TextView = view.findViewById(R.id.tvNota)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
        val layoutNota: LinearLayout = view.findViewById(R.id.layoutNotaColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calificacion, parent, false)
        return CalificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalificacionViewHolder, position: Int) {
        val calif = lista[position]
        holder.tvTitulo.text = calif.titulo
        holder.tvTipo.text = calif.tipo
        holder.tvNota.text = "Nota: ${calif.nota}"
        holder.tvFecha.text = "Fecha: ${calif.fecha}"

        // Color según nota
        val context = holder.itemView.context
        val color = when {
            calif.nota < 5 -> R.color.red
            calif.nota in 5.0..5.9 -> R.color.orange
            else -> R.color.green
        }
        holder.layoutNota.setBackgroundColor(ContextCompat.getColor(context, color))

        // Acciones
        if (!esProfesor) { //Si es Alumno los botones de Editar y Eliminar se ocultan
            holder.btnEditar.visibility = View.GONE
            holder.btnEliminar.visibility = View.GONE
        } else {
            holder.btnEditar.setOnClickListener { onEditarClick(calif) }
            holder.btnEliminar.setOnClickListener { onBorrarClick(calif) }
        }

    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Calificacion>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}

