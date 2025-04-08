package com.example.navegacion.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentLoginBinding
import com.example.navegacion.ui.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    // Inicializo auth
    override fun onAttach(context: Context) {
        super.onAttach(context)
        auth = FirebaseAuth.getInstance()
    }

    // Asocia parte gráfica (layout) con la lógica
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Configurar el Spinner
        val spinner = binding.SpinnerId

        // (Opcional) OnItemSelectedListener si necesitas hacer algo al cambiar de selección
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val seleccionado = parentView?.getItemAtPosition(position).toString()
                Log.d("LoginFragment", "Seleccionado: $seleccionado")
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Lógica cuando no se selecciona nada (opcional)
            }
        }

        // Botón Iniciar Sesión
        binding.btnLogin.setOnClickListener {
            val correo = binding.editCorreo.text.toString()
            val pass = binding.editPass.text.toString()

            auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Aquí decides a qué fragment ir según la selección del Spinner
                        val seleccionado = spinner.selectedItem.toString()

                        // (Opcional) Para pasar datos al siguiente fragment
                        val bundle = Bundle()
                        val usuario = User(correo, pass)
                        bundle.putSerializable("usuario", usuario)

                        when (seleccionado) {
                            "Alumno" -> {
                                // Ir a Home Alumno
                                findNavController().navigate(
                                    R.id.action_loginFragment_to_homeAlumnoFragment,
                                    bundle
                                )
                            }
                            "Profesor" -> {
                                // Ir a Home Profesor
                                findNavController().navigate(
                                    R.id.action_loginFragment_to_homeProfesorFragment,
                                    bundle
                                )
                            }
                        }
                    } else {
                        // Error al iniciar sesión
                        Snackbar.make(binding.root, "Error al iniciar sesión", Snackbar.LENGTH_SHORT)
                            .setAction("¿Quieres registrarte?") {
                                findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
                            }
                            .show()
                    }
                }
        }

        // Botón Registro
        binding.btnRegistro.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }
    }
}
