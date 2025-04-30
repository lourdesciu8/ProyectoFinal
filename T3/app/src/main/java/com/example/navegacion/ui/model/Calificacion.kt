package com.example.navegacion.ui.model

data class Calificacion(
    var profesorUID: String = "",
    var nombreProfesor: String = "",
    var alumnoUID: String = "",
    var nombreAlumno: String = "",
    var titulo: String = "",
    var tipo: String = "",
    var nota: Double = 0.0,
    var fecha: String = "",
    var modulo: String = "",
    var id: String? = null
)
