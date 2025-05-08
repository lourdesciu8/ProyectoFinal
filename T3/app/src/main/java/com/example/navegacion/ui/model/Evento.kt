package com.example.navegacion.ui.model



data class Evento(
    val id: String? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val fecha: Long? = null,
    val creadoPor: String? = null,
    val asignadoA: Map<String, Boolean>? = null,
    val esPersonal: Boolean = false,
    val modulo: String? = null
)




