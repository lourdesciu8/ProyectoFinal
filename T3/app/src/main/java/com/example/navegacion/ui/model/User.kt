package com.example.navegacion.ui.model

import java.io.Serializable

//Se igualan a null para tener constructor vacío para el mapeo
class User(var correo: String? = null,
           var nombre: String? = null,
    ) : Serializable {
}
