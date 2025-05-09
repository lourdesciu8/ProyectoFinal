package com.example.navegacion.ui.fragment
import android.view.View
import android.widget.TextView
import com.kizitonwose.calendar.view.ViewContainer
import com.example.navegacion.R
import com.kizitonwose.calendar.core.CalendarDay

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.dayText)
    lateinit var day: CalendarDay
}