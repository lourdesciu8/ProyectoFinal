package com.example.navegacion.ui.model

data class Mensaje(
    val idMensaje: String = "",    // ID del mensaje (clave en la base de datos)
    val texto: String = "",        // Contenido del mensaje
    val autor: String = "",        // UID del autor del mensaje
    val marcaTemporal: Long = 0L   // Fecha de envío en milisegundos
)