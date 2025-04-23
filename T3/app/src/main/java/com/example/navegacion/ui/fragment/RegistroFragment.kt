package com.example.navegacion.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentRegistroBinding
import com.example.navegacion.ui.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistroFragment : Fragment() {
    private lateinit var binding: FragmentRegistroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onAttach(context: Context) {
        super.onAttach(context)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app/")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.btnLogin.setOnClickListener {
            val correo = binding.editCorreo.text.toString()
            val contrasena = binding.editPass.text.toString()

            auth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        guardarUsuario(
                            User(
                                correo = correo,
                                nombre = binding.editNombre.text.toString(),
                                rol = binding.spinnerRol.selectedItem.toString()
                            )
                        )

                        // Se crea el Bundle para enviar correo y pass al fragmento de login
                        val bundle = Bundle().apply {
                            putString("correo", correo)
                            putString("contrasena", contrasena)
                        }

                        // se navega al login con el bundle
                        findNavController().navigate(
                            R.id.action_registroFragment_to_loginFragment,
                            bundle
                        )
                    } else {
                        Snackbar.make(binding.root, "Error en el registro", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    private fun guardarUsuario(usuario: User) {
        database.reference.child("usuarios").child(auth.currentUser!!.uid).setValue(usuario)
    }
}
