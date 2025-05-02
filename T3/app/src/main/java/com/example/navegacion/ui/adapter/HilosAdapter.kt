package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.databinding.ItemHiloBinding
import com.example.navegacion.ui.model.Hilo
import java.text.SimpleDateFormat
import java.util.*

class HilosAdapter(
    private val items: List<Hilo>,
    private val onClick: (Hilo) -> Unit
) : RecyclerView.Adapter<HilosAdapter.HiloVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiloVH {
        val binding = ItemHiloBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HiloVH(binding)
    }

    override fun onBindViewHolder(holder: HiloVH, position: Int) {
        val hilo = items[position]
        holder.binding.tvTituloHilo.text = hilo.titulo

        // Formatear la fecha de creación
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = sdf.format(Date(hilo.marcaTemporal))
        holder.binding.tvInfoHilo.text = "Creado por ${hilo.creadoPor} el $fecha"

        holder.itemView.setOnClickListener { onClick(hilo) }
    }

    override fun getItemCount() = items.size

    class HiloVH(val binding: ItemHiloBinding) : RecyclerView.ViewHolder(binding.root)
}
