package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentRegistroBinding

class RegistroFragment : Fragment () { //Tiene que heredar de Fragment
    private lateinit var binding: FragmentRegistroBinding

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
            findNavController()
        }
    }
}