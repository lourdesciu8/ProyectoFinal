package com.example.navegacion.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class CalendarAdapter(
    private val daysList: List<Pair<String, Boolean>>, // Asegúrate de que el parámetro está correctamente definido
    private val onDayClick: (String) -> Unit // Función para manejar el clic en cada día
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val (day, hasEvent) = daysList[position]
        holder.dayText.text = day

        // Si el día tiene eventos, cambiar el color de fondo
        if (hasEvent) {
            holder.dayText.setBackgroundResource(R.drawable.circle_personal)
        }

        // Manejar clic en el día para navegar a CalendarioFragment
        holder.itemView.setOnClickListener {
            onDayClick(day)
        }
    }

    override fun getItemCount(): Int = daysList.size
}