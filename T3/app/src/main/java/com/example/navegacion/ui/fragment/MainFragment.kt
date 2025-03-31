package com.example.navegacion.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentLoginBinding
import com.example.navegacion.databinding.FragmentMainBinding
import com.example.navegacion.ui.model.User

class MainFragment : Fragment () { //Tiene que heredar de Fragment
    private lateinit var binding: FragmentMainBinding
    private var usuario: User?=null;

    //El usuario le recupero en el primer metodo del ciclo de vida (onAttach) de un fragment
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(arguments?.getSerializable("usuario") !=null)
            this.usuario=arguments?.getSerializable("usuario") as User
    }

    //Unico metodo obligatorio:asocia parte gráfica y lógica
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.textMain.setText("Iniciado como ${usuario?.correo ?: "Invitado"}")
        binding.btnVolver.setOnClickListener {
            findNavController()
    }
}

}