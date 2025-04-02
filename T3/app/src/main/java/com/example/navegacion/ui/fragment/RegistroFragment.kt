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

class RegistroFragment : Fragment () { //Tiene que heredar de Fragment
    private lateinit var binding: FragmentRegistroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    //Primer metodo ciclo vida de un fragment
    override fun onAttach(context: Context) {
        super.onAttach(context)
        auth=FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app/")
    }

    //Unico metodo obligatorio:asocia parte gráfica y lógica
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentRegistroBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.btnLogin.setOnClickListener {
            auth.createUserWithEmailAndPassword(
                binding.editCorreo.text.toString(),
                binding.editPass.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    // guardar los datos del usuario en base de datos
                    guardarUsuario(
                        User(
                            correo = binding.editCorreo.text.toString(),
                            nombre = binding.editNombre.text.toString(),
                        )
                    )
                    //Navegar hacia el mainFragment
                    findNavController().navigate(R.id.action_registroFragment_to_mainFragment)
                } else {
                    Snackbar.make(binding.root, "Error en el registro, usuario registrado", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun guardarUsuario(usuario: User) {
        database.reference.child("usuarios").child(auth.currentUser!!.uid).setValue(usuario)

    }
}