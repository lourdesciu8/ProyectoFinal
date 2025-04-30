package com.example.navegacion.ui.model

data class Calificacion(
    var id: String? = null,
    var titulo: String = "",
    var tipo: String = "",
    var nota: Double = 0.0,
    var fecha: String = "",
    var profesorUID: String = "",
    var modulo: String = "",
    var alumnoUID: String = ""
)
