package com.example.navegacion.ui.model

data class Hilo(
    val idHilo: String = "",       // ID del hilo (clave en la base de datos)
    val titulo: String = "",       // Título del hilo
    val creadoPor: String = "",    // UID del autor
    val marcaTemporal: Long = 0L   // Fecha de creación en milisegundos
)