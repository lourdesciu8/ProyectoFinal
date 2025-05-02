// src/main/java/com/example/navegacion/ui/adapter/HilosAdapter.kt
package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.databinding.ItemHiloBinding
import com.example.navegacion.ui.model.Hilo

class HilosAdapter(
    private val items: List<Hilo>,
    private val userMap: Map<String, String>,      // uid → nombre
    private val onClick: (Hilo) -> Unit
) : RecyclerView.Adapter<HilosAdapter.HiloVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiloVH {
        val binding = ItemHiloBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HiloVH(binding)
    }

    override fun onBindViewHolder(holder: HiloVH, position: Int) {
        val hilo = items[position]
        holder.binding.tvTituloHilo.text = hilo.titulo

        // Mapea UID→nombre
        val autorNombre = userMap[hilo.creadoPor] ?: hilo.creadoPor
        holder.binding.tvInfoHilo.text = autorNombre

        holder.binding.root.setOnClickListener { onClick(hilo) }
    }

    override fun getItemCount() = items.size

    class HiloVH(val binding: ItemHiloBinding) : RecyclerView.ViewHolder(binding.root)
}
