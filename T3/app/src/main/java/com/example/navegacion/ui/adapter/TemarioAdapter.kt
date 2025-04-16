package com.example.navegacion.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.model.Temario

class TemarioAdapter(
    private var listaTemarios: MutableList<Temario>,
    private val esProfesor: Boolean = false,
    private val onEliminarClick: ((Temario) -> Unit)? = null
) : RecyclerView.Adapter<TemarioAdapter.TemarioViewHolder>() {


    inner class TemarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombreTemario)
        val btnVer: Button = itemView.findViewById(R.id.btnVerTemario)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarTemario)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_temario, parent, false)
        return TemarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemarioViewHolder, position: Int) {
        val temario = listaTemarios[position]
        holder.nombre.text = temario.nombreArchivo
        holder.btnVer.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(temario.url))
            it.context.startActivity(intent)
        }

        // Mostrar u ocultar el botón de eliminar
        holder.btnEliminar.visibility = if (esProfesor) View.VISIBLE else View.GONE
        holder.btnEliminar.setOnClickListener {
            onEliminarClick?.invoke(temario)
        }

    }

    override fun getItemCount(): Int = listaTemarios.size

    fun actualizarLista(nuevaLista: List<Temario>) {
        listaTemarios.clear()
        listaTemarios.addAll(nuevaLista)
        notifyDataSetChanged()
    }

}

