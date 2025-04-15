package com.example.navegacion.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

// Usamos object para mantener una sola instancia (singleton)
object FirebaseStorageHelper {

    fun subirArchivo(
        context: Context,
        uri: Uri,
        nombreArchivo: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val archivoRef = storageRef.child("temarios/$nombreArchivo")

        archivoRef.putFile(uri)
            .addOnSuccessListener {
                archivoRef.downloadUrl.addOnSuccessListener { url ->
                    Log.d("RealtimeDB", "Llamando a guardarEnRealtimeDatabase")
                    guardarEnRealtimeDatabase(context, nombreArchivo, url.toString())
                    Toast.makeText(context, "Archivo subido correctamente", Toast.LENGTH_SHORT).show()
                    onSuccess(url.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                onError()
            }
    }

    //Funcion que guarda automaticamente en la bbdd el nodo temarios con el nombre del archivo y la url de descarga
    private fun guardarEnRealtimeDatabase(context: Context, nombreArchivo: String, url: String) {
        val dbRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app").getReference("temarios")
        val nuevoTemario = hashMapOf(
            "nombreArchivo" to nombreArchivo,
            "url" to url
        )

        dbRef.push().setValue(nuevoTemario)
            .addOnSuccessListener {
                Toast.makeText(context, "Temario guardado en la base de datos", Toast.LENGTH_SHORT).show()
                //Log.d("RealtimeDB", "Temario guardado en Realtime Database")
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al guardar el temario en la base de datos", Toast.LENGTH_SHORT).show()
                //Log.e("RealtimeDB", "Error al guardar en Realtime Database: ${error.message}")
            }

    }


}