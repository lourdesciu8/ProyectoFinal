package com.example.navegacion.ui.model



data class Evento(
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
   // val asignadoA: String? = null,
    val id: String? = null,
    val timestamp: Long? = null // ← este es el nuevo campo
)



