package com.example.navegacion.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

// Usamos object para mantener una sola instancia (singleton)
object FirebaseStorageHelper {

    fun subirArchivo(
        context: Context,
        uri: Uri,
        nombreArchivo: String,
        modulo: String,
        carpeta: String, // "temarios" o "examenes"
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val archivoRef = storageRef.child("$carpeta/$modulo/$nombreArchivo")

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

    //funcion para guardar en la base de datos los archivos subidos por el profesor
    fun guardarEnRealtimeDatabase(
        context: Context,
        nombreArchivo: String,
        url: String,
        modulo: String,
        nodo: String = "temarios" // por defecto guarda en "temarios", pero se puede cambiar a "examenes"
    ) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uid = user?.uid ?: return

        // Recuperar nombre del profesor del nodo usuarios
        val usuarioRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("usuarios")
            .child(uid)

        usuarioRef.get().addOnSuccessListener { snapshot ->
            val nombreProfesor = snapshot.child("nombre").value?.toString() ?: "Desconocido"

            val nuevoArchivo = hashMapOf(
                "nombreArchivo" to nombreArchivo,
                "url" to url,
                "uid" to uid,
                "nombreProfesor" to nombreProfesor,
                "modulo" to modulo
            )

            val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference(nodo)

            dbRef.push().setValue(nuevoArchivo)
                .addOnSuccessListener {
                    // Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show()
                }

        }.addOnFailureListener {
            Toast.makeText(context, "No se pudo obtener el nombre del profesor", Toast.LENGTH_SHORT).show()
        }
    }
}
