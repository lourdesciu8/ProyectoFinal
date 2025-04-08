package com.example.navegacion.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarioViewModel : ViewModel() {
    // Mapa que guarda eventos por fecha (clave: timestamp de la fecha, valor: lista de eventos)
    private val eventosMap = MutableLiveData<MutableMap<Long, MutableList<String>>>().apply { value = mutableMapOf() }

    // Método para agregar un evento a una fecha específica
    fun agregarEvento(fecha: Long, evento: String) {
        val eventos = eventosMap.value ?: mutableMapOf()
        eventos.getOrPut(fecha) { mutableListOf() }.add(evento)
        eventosMap.value = eventos
    }

    // Método para obtener los eventos guardados
    fun obtenerEventos(): LiveData<MutableMap<Long, MutableList<String>>> = eventosMap
}
