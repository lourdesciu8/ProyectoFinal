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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class RegistroFragment : Fragment () { //Tiene que heredar de Fragment
    private lateinit var binding: FragmentRegistroBinding
    private lateinit var auth: FirebaseAuth

    //Primer metodo ciclo vida de un fragment
    override fun onAttach(context: Context) {
        super.onAttach(context)
        auth=FirebaseAuth.getInstance()
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
                binding.editCorreo.text.toString(), binding.editPass.text.toString()
            ).addOnCompleteListener {
                if(it.isSuccessful){
                    findNavController().navigate(R.id.action_registroFragment_to_mainFragment)
                }else{
                    Snackbar.make(binding.root, "Error en el registro", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }
}