package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentLoginBinding
import com.example.navegacion.ui.model.User

class LoginFragment : Fragment() { // Tiene que heredar de Fragment
    private lateinit var binding: FragmentLoginBinding

    // Único método obligatorio: asocia parte gráfica y lógica
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Configurar el Spinner
        val spinner = binding.SpinnerId // Accedemos al Spinner por su ID

        // Establecer el OnItemSelectedListener para el Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Obtener el valor seleccionado del Spinner
                val seleccionado = parentView?.getItemAtPosition(position).toString()

                // Aquí hay que agregar la lógica para "Alumno" o "Profesor"
                when (seleccionado) {
                    "Alumno" -> {
                        // Lógica para Alumno: que se redirija hacia fragmet de Alumno
                    }
                    "Profesor" -> {
                        // Lógica para Profesor: que se redirija hacia fragmet de Profesor
                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Lógica cuando no se selecciona nada (opcional)
            }
        }

        // Configurar los botones (login y registro)
        binding.btnLogin.setOnClickListener {
            val bundle = Bundle() // Para pasar a pantalla main el nombre del usuario iniciado
            val usuario = User(binding.editCorreo.text.toString(), binding.editPass.text.toString())
            bundle.putSerializable("usuario", usuario)
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment, bundle)
        }

       /* binding.btnRegistro.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }*/
    }
}
