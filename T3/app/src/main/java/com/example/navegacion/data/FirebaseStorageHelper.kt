package com.example.navegacion.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

// Usamos object para mantener una sola instancia (singleton)
object FirebaseStorageHelper {

    //Esta función debe únicamente subir el archivo y devolver la URL.
    fun subirArchivo(
        context: Context,
        uri: Uri,
        nombreArchivo: String,
        modulo: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    )
    {
        val storageRef = FirebaseStorage.getInstance().reference
        val archivoRef = storageRef.child("temarios/$modulo/$nombreArchivo") //Guardar en storage agrupado por módulo

        archivoRef.putFile(uri)
            .addOnSuccessListener {
                archivoRef.downloadUrl.addOnSuccessListener { url ->
                    onSuccess(url.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                onError()
            }
    }

    //Funcion que guarda automaticamente en la bbdd el nodo temarios con el nombre del archivo y la url de descarga
    fun guardarEnRealtimeDatabase(
        context: Context,
        nombreArchivo: String,
        url: String,
        modulo: String
    ) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uid = user?.uid ?: return

        // Recuperamos el nombre del profesor desde el nodo usuarios
        val usuarioRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("usuarios")
            .child(uid)

        usuarioRef.get().addOnSuccessListener { snapshot ->
            val nombreProfesor = snapshot.child("nombre").value?.toString() ?: "Desconocido"

            val nuevoTemario = hashMapOf(
                "nombreArchivo" to nombreArchivo,
                "url" to url,
                "uid" to uid,
                "nombreProfesor" to nombreProfesor,
                "modulo" to modulo // <-- guardamos el módulo
            )

            val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("temarios")

            dbRef.push().setValue(nuevoTemario)
                .addOnSuccessListener {
                    //Toast.makeText(context, "Temario guardado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al guardar el temario", Toast.LENGTH_SHORT).show()
                }

        }.addOnFailureListener {
            Toast.makeText(context, "No se pudo obtener el nombre del profesor", Toast.LENGTH_SHORT).show()
        }
    }

}