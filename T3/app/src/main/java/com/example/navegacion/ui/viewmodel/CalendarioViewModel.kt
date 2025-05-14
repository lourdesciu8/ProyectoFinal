package com.example.navegacion.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navegacion.ui.model.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CalendarioViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app/")

    private val _eventosAlumno = MutableLiveData<List<Evento>>()
    val eventosAlumno: LiveData<List<Evento>> get() = _eventosAlumno

    private val _eventosProfesor = MutableLiveData<List<Evento>>()
    val eventosProfesor: LiveData<List<Evento>> get() = _eventosProfesor

    /*fun cargarEventosAlumno(uid: String) {
        val ref = database.reference.child("eventos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Evento>()
                for (eventoSnap in snapshot.children) {
                    val evento = eventoSnap.getValue(Evento::class.java)
                    if (evento != null && evento.asignadoA?.containsKey(uid) == true) {
                        lista.add(evento.copy(id = eventoSnap.key))
                    }
                }
                _eventosAlumno.postValue(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                _eventosAlumno.postValue(emptyList())
            }
        })
    }*/
    fun cargarEventosAlumno(uid: String) {
        val ref = database.reference.child("eventos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Evento>()
                for (eventoSnap in snapshot.children) {
                    val evento = eventoSnap.getValue(Evento::class.java)
                    if (evento != null && (
                                evento.asignadoA?.containsKey(uid) == true ||
                                        (evento.esPersonal == true && evento.creadoPor == uid)
                                )) {
                        lista.add(evento.copy(id = eventoSnap.key))
                    }
                }
                _eventosAlumno.postValue(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                _eventosAlumno.postValue(emptyList())
            }
        })
    }


    fun cargarEventosProfesor(uid: String) {
        val ref = database.reference.child("eventos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Evento>()
                for (eventoSnap in snapshot.children) {
                    val evento = eventoSnap.getValue(Evento::class.java)
                    if (evento != null && evento.creadoPor == uid) {
                        lista.add(evento.copy(id = eventoSnap.key))
                    }
                }
                _eventosProfesor.postValue(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                _eventosProfesor.postValue(emptyList())
            }
        })
    }

    fun obtenerModuloDelProfesorNombre(callback: (String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(null)
        val ref = database.reference.child("modulos")

        ref.get().addOnSuccessListener { snapshot ->
            for (moduloSnap in snapshot.children) {
                val profesoresSnap = moduloSnap.child("Profesores")
                if (profesoresSnap.hasChild(uid)) {
                    val nombreModulo = moduloSnap.child("nombre").getValue(String::class.java)
                    callback(nombreModulo) // ← Devuelve el valor del campo "nombre"
                    return@addOnSuccessListener
                }
            }
            callback(null)
        }
    }

    fun obtenerModuloDelProfesor(callback: (String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(null)
        val ref = database.reference.child("modulos")

        ref.get().addOnSuccessListener { snapshot ->
            for (moduloSnap in snapshot.children) {
                val profesoresSnap = moduloSnap.child("Profesores")
                if (profesoresSnap.hasChild(uid)) {
                    callback(moduloSnap.key)
                    return@addOnSuccessListener
                }
            }
            callback(null)
        }
    }



}



