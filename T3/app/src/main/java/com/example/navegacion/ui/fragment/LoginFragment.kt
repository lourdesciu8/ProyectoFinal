package com.example.navegacion.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentLoginBinding
import com.example.navegacion.ui.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar el login con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // <- este es mi client_id de strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val currentUser = auth.currentUser
                                val uid = currentUser?.uid
                                val rolSeleccionado = binding.SpinnerId.selectedItem.toString()

                                //User busca su uid y consulta el nodo usuarios/uid
                                val databaseRef = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
                                    .getReference("usuarios").child(uid!!)

                                databaseRef.get().addOnSuccessListener { dataSnapshot ->
                                    val rolEnFirebase = dataSnapshot.child("rol").value.toString()

                                    //Se compara el rol guardado en el nodo con el del Spinner
                                    if (rolEnFirebase == rolSeleccionado) {
                                        val correo = currentUser.email ?: ""
                                        val bundle = Bundle().apply {
                                            putSerializable("usuario", User(correo, "", rolEnFirebase))
                                        }

                                        //Si el rol coincide, se navega al fragment correspondiente
                                        when (rolSeleccionado) {
                                            "Alumno" -> findNavController().navigate(R.id.action_loginFragment_to_homeAlumnoFragment, bundle)
                                            "Profesor" -> findNavController().navigate(R.id.action_loginFragment_to_homeProfesorFragment, bundle)
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), "Rol incorrecto. Selecciona el rol correcto.", Toast.LENGTH_SHORT).show()
                                        FirebaseAuth.getInstance().signOut()
                                    }
                                }

                            } else {
                                Toast.makeText(requireContext(), "Fallo en el login con Google", Toast.LENGTH_SHORT).show()
                            }
                        }
                } catch (e: ApiException) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

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

        // Spinner
        val spinner = binding.SpinnerId

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccionado = parentView?.getItemAtPosition(position).toString()
                Log.d("LoginFragment", "Seleccionado: $seleccionado")
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        // Iniciar sesión con correo y contraseña
        binding.btnLogin.setOnClickListener {
            val correo = binding.editCorreo.text.toString()
            val pass = binding.editPass.text.toString()

            auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        val rolSeleccionado = spinner.selectedItem.toString()

                        val databaseRef = FirebaseDatabase
                            .getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
                            .getReference("usuarios")

                        //Una vez que el usuario se loguea correctamente, busca su uid y consulta el nodo usuarios/uid
                        databaseRef.child(uid!!).get().addOnSuccessListener { dataSnapshot ->
                            val rolEnFirebase = dataSnapshot.child("rol").getValue(String::class.java)

                            //Compara el rol guardado en el nodo con el del Spinner
                            if (rolEnFirebase == rolSeleccionado) {
                                val bundle = Bundle().apply {
                                    putSerializable("usuario", User(binding.editCorreo.text.toString(), "", rolSeleccionado))
                                }

                                //Si el rol coincide, puede entrar al fragment correspondiente (navega)
                                when (rolSeleccionado) {
                                    "Alumno" -> findNavController().navigate(R.id.action_loginFragment_to_homeAlumnoFragment, bundle)
                                    "Profesor" -> findNavController().navigate(R.id.action_loginFragment_to_homeProfesorFragment, bundle)
                                }
                            } else {
                                auth.signOut()
                                Toast.makeText(requireContext(), "El rol seleccionado no coincide con tu cuenta.", Toast.LENGTH_LONG).show()
                            }

                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Error al obtener datos del usuario.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        Snackbar.make(binding.root, "Error al iniciar sesión", Snackbar.LENGTH_SHORT)
                            .setAction("¿Quieres registrarte?") {
                                findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
                            }
                            .show()
                    }
                }
        }

        // Botón registro
        binding.btnRegistro.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }

        // ¿Has olvidado tu contraseña?
        binding.txtOlvidasteContrasena.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recuperarContrasenaFragment)
        }


        // Botón login con Google
        //Al pulsar el botón, te preguntará qué cuenta quieres usar, la ya logeada u otra distinta
        binding.btnGoogleSignIn.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                launcher.launch(googleSignInClient.signInIntent)
            }
        }

    }
}
