// src/main/java/com/example/navegacion/ui/adapter/MensajesAdapter.kt
package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.databinding.ItemMensajeBinding
import com.example.navegacion.ui.model.Mensaje
import java.text.SimpleDateFormat
import java.util.*

class MensajesAdapter(
    private val items: List<Mensaje>,
    private val userMap: Map<String, String>  // uid → nombre
) : RecyclerView.Adapter<MensajesAdapter.MensajeVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeVH {
        val binding = ItemMensajeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MensajeVH(binding)
    }

    override fun onBindViewHolder(holder: MensajeVH, position: Int) {
        val msg = items[position]
        holder.binding.tvTextoMensaje.text = msg.texto

        // Mapea UID → nombre (o usa UID si no existe)
        val autorNombre = userMap[msg.autor] ?: msg.autor
        // Formateador con zona horaria de Madrid
        val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Europe/Madrid")
        }
        val fechaFormateada = sdf.format(Date(msg.marcaTemporal))

        holder.binding.tvInfoMensaje.text = "$autorNombre • $fechaFormateada"
    }


    override fun getItemCount() = items.size

    class MensajeVH(val binding: ItemMensajeBinding) : RecyclerView.ViewHolder(binding.root)
}
