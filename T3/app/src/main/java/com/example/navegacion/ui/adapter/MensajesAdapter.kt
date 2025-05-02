package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.databinding.ItemMensajeBinding
import com.example.navegacion.ui.model.Mensaje
import java.text.SimpleDateFormat
import java.util.*

class MensajesAdapter(
    private val items: List<Mensaje>
) : RecyclerView.Adapter<MensajesAdapter.MensajeVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeVH {
        val binding = ItemMensajeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MensajeVH(binding)
    }

    override fun onBindViewHolder(holder: MensajeVH, position: Int) {
        val msg = items[position]
        holder.binding.tvTextoMensaje.text = msg.texto

        // Formatear marcaTemporal a "HH:mm dd/MM/yyyy"
        val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        holder.binding.tvInfoMensaje.text =
            "${msg.autor} • ${sdf.format(Date(msg.marcaTemporal))}"
    }

    override fun getItemCount(): Int = items.size

    inner class MensajeVH(val binding: ItemMensajeBinding) : RecyclerView.ViewHolder(binding.root)
}
