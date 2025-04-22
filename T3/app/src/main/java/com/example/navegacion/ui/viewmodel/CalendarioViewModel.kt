package com.example.navegacion.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navegacion.ui.model.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CalendarioViewModel : ViewModel() {

    private val eventosMap = MutableLiveData<MutableMap<Long, MutableList<Evento>>>()
        .apply { value = mutableMapOf() }

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app/")

    // Agregar un evento (local + Firebase)
   /* fun agregarEvento(fecha: Long, evento: Evento) {
        val uid = auth.currentUser?.uid ?: return
        val fechaKey = fecha.toString()

        val ref = database.reference
            .child("eventos")
            .child(uid)
            .child(fechaKey)
            .push()

        val eventoConId = evento.copy(id = ref.key)
        ref.setValue(eventoConId)

        // Local
        val eventos = eventosMap.value ?: mutableMapOf()
        eventos.getOrPut(fecha) { mutableListOf() }.add(eventoConId)
        eventosMap.value = eventos
    }*/
    fun agregarEvento(fecha: Long, evento: Evento) {
        val uid = auth.currentUser?.uid ?: return
        val fechaKey = fecha.toString()

        val ref = database.reference
            .child("eventos")
            .child(uid)
            .child(fechaKey)
            .push()

        val eventoConIdYFecha = evento.copy(id = ref.key, timestamp = fecha)
        ref.setValue(eventoConIdYFecha)

        // Local
        val eventos = eventosMap.value ?: mutableMapOf()
        eventos.getOrPut(fecha) { mutableListOf() }.add(eventoConIdYFecha)
        eventosMap.value = eventos
    }


    // Eliminar (Firebase + local)
    fun eliminarEvento(fecha: Long, evento: Evento) {
        val uid = auth.currentUser?.uid ?: return
        val eventoId = evento.id ?: return

        // Firebase
        database.reference
            .child("eventos")
            .child(uid)
            .child(fecha.toString())
            .child(eventoId)
            .removeValue()

        // Local
        eventosMap.value?.get(fecha)?.remove(evento)
        eventosMap.postValue(eventosMap.value)
    }

    // Editar (Firebase + local)
    fun editarEvento(fecha: Long, viejo: Evento, nuevo: Evento) {
        val uid = auth.currentUser?.uid ?: return
        val eventoId = viejo.id ?: return

        val actualizado = nuevo.copy(id = eventoId)

        // Firebase
        database.reference
            .child("eventos")
            .child(uid)
            .child(fecha.toString())
            .child(eventoId)
            .setValue(actualizado)

        // Local
        val lista = eventosMap.value?.get(fecha)
        val index = lista?.indexOf(viejo)
        if (index != null && index >= 0) {
            lista[index] = actualizado
            eventosMap.postValue(eventosMap.value)
        }
    }

    // Obtener eventos
    fun obtenerEventos(): LiveData<MutableMap<Long, MutableList<Evento>>> = eventosMap

    // Cargar desde Firebase al iniciar
    fun cargarEventosDesdeFirebase() {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.reference.child("eventos").child(uid)

        ref.get().addOnSuccessListener { snapshot ->
            val map = mutableMapOf<Long, MutableList<Evento>>()

            for (fechaSnap in snapshot.children) {
                val fecha = fechaSnap.key?.toLongOrNull() ?: continue
                val eventos = mutableListOf<Evento>()

                for (eventoSnap in fechaSnap.children) {
                    val evento = eventoSnap.getValue(Evento::class.java)
                    evento?.let { eventos.add(it) }
                }

                map[fecha] = eventos
            }

            eventosMap.value = map
        }
    }
}
