package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentRecuperarcontrasenaBinding
import com.google.firebase.auth.FirebaseAuth

class RecuperarContrasenaFragment : Fragment() {

    private lateinit var binding: FragmentRecuperarcontrasenaBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecuperarcontrasenaBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        binding.btnEnviarRecuperacion.setOnClickListener {
            val correo = binding.editCorreoRecuperacion.text.toString().trim()

            if (correo.isNotEmpty()) {
                auth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Correo enviado. Revisa tu bandeja de entrada.",
                                Toast.LENGTH_LONG
                            ).show()
                            // Navegamos de vuelta al login después 2 seg de espera
                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController().navigate(R.id.action_recuperarContrasenaFragment_to_loginFragment)
                            }, 2000) // 2 segundos de espera

                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error al enviar el correo. ¿Está bien escrito?",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Introduce un correo válido", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}
