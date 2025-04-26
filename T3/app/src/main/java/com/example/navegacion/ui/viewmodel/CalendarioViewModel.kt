package com.example.navegacion.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navegacion.ui.model.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase





class CalendarioViewModel : ViewModel() {

    // 🔵 Eventos personales (alumno)
    private val eventosUsuarioMap = MutableLiveData<MutableMap<Long, MutableList<Evento>>>()
        .apply { value = mutableMapOf() }

    // 🔵 Eventos de módulo (profesor)
    private val eventosModuloMap = MutableLiveData<MutableMap<Long, MutableList<Evento>>>()
        .apply { value = mutableMapOf() }

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app/")

    // 🔵 ALUMNO: Agregar evento personal
    fun agregarEvento(fecha: Long, evento: Evento) {
        val uid = auth.currentUser?.uid ?: return
        val fechaKey = fecha.toString()

        val ref = database.reference
            .child("eventos_usuario")
            .child(uid)
            .child(fechaKey)
            .push()

        val eventoConId = evento.copy(id = ref.key)
        ref.setValue(eventoConId)

        val eventos = eventosUsuarioMap.value ?: mutableMapOf()
        eventos.getOrPut(fecha) { mutableListOf() }.add(eventoConId)
        eventosUsuarioMap.value = eventos
    }

    // 🔵 PROFESOR: Agregar evento a módulo
    fun agregarEventoModulo(fecha: Long, evento: Evento, modulo: String) {
        val fechaKey = fecha.toString()

        val ref = database.reference
            .child("eventos_modulo")
            .child(modulo)
            .child(fechaKey)
            .push()

        val eventoConId = evento.copy(id = ref.key)
        ref.setValue(eventoConId)

        val eventos = eventosModuloMap.value ?: mutableMapOf()
        eventos.getOrPut(fecha) { mutableListOf() }.add(eventoConId)
        eventosModuloMap.value = eventos
    }

    // 🔵 ALUMNO: Editar evento personal
    fun editarEvento(fecha: Long, viejo: Evento, nuevo: Evento) {
        val uid = auth.currentUser?.uid ?: return
        val eventoId = viejo.id ?: return

        val actualizado = nuevo.copy(id = eventoId)

        database.reference
            .child("eventos_usuario")
            .child(uid)
            .child(fecha.toString())
            .child(eventoId)
            .setValue(actualizado)

        val lista = eventosUsuarioMap.value?.get(fecha)
        val index = lista?.indexOf(viejo)
        if (index != null && index >= 0) {
            lista[index] = actualizado
            eventosUsuarioMap.postValue(eventosUsuarioMap.value)
        }
    }

    // 🔵 PROFESOR: Editar evento de módulo
    fun editarEventoModulo(fecha: Long, viejo: Evento, nuevo: Evento, modulo: String) {
        val eventoId = viejo.id ?: return

        val actualizado = nuevo.copy(id = eventoId)

        database.reference
            .child("eventos_modulo")
            .child(modulo)
            .child(fecha.toString())
            .child(eventoId)
            .setValue(actualizado)

        val lista = eventosModuloMap.value?.get(fecha)
        val index = lista?.indexOf(viejo)
        if (index != null && index >= 0) {
            lista[index] = actualizado
            eventosModuloMap.postValue(eventosModuloMap.value)
        }
    }

    // 🔵 ALUMNO: Eliminar evento personal
    fun eliminarEvento(fecha: Long, evento: Evento) {
        val uid = auth.currentUser?.uid ?: return
        val eventoId = evento.id ?: return

        database.reference
            .child("eventos_usuario")
            .child(uid)
            .child(fecha.toString())
            .child(eventoId)
            .removeValue()

        eventosUsuarioMap.value?.get(fecha)?.remove(evento)
        eventosUsuarioMap.postValue(eventosUsuarioMap.value)
    }

    // 🔵 PROFESOR: Eliminar evento de módulo
    fun eliminarEventoModulo(fecha: Long, evento: Evento, modulo: String) {
        val eventoId = evento.id ?: return

        database.reference
            .child("eventos_modulo")
            .child(modulo)
            .child(fecha.toString())
            .child(eventoId)
            .removeValue()

        eventosModuloMap.value?.get(fecha)?.remove(evento)
        eventosModuloMap.postValue(eventosModuloMap.value)
    }

    // 🔵 ALUMNO: Cargar eventos personales
    fun cargarEventosDesdeFirebase() {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.reference.child("eventos_usuario").child(uid)

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

            eventosUsuarioMap.value = map
        }
    }

    // 🔵 PROFESOR: Cargar eventos de módulo
    fun cargarEventosModuloDesdeFirebase(modulo: String) {
        val ref = database.reference.child("eventos_modulo").child(modulo)

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

            eventosModuloMap.value = map
        }
    }

    fun obtenerModuloDelProfesor(callback: (String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(null)
        val ref = database.reference.child("modulos")

        ref.get().addOnSuccessListener { snapshot ->
            for (moduloSnap in snapshot.children) {
                val profesoresSnap = moduloSnap.child("Profesores")
                if (profesoresSnap.hasChild(uid)) {
                    callback(moduloSnap.key) // Retorna el nombre del módulo (mod1, mod2, etc)
                    return@addOnSuccessListener
                }
            }
            callback(null) // No encontrado
        }
    }


    // 🔵 Exponer los LiveData
    fun obtenerEventos(): LiveData<MutableMap<Long, MutableList<Evento>>> = eventosUsuarioMap
    fun obtenerEventosModulo(): LiveData<MutableMap<Long, MutableList<Evento>>> = eventosModuloMap
}

