package com.example.navegacion.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.navegacion.R
import com.example.navegacion.data.FirebaseStorageHelper
import com.google.firebase.auth.FirebaseAuth

class TemarioProfesorFragment : Fragment() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_temarioprofesor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón de volver
        view.findViewById<ImageButton>(R.id.btnVolver)?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Botón para seleccionar archivo
        val botonSeleccionar = view.findViewById<Button>(R.id.btnSeleccionarArchivo)
        botonSeleccionar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"  //Se fitra el tipo de archivo
            filePickerLauncher.launch(intent)
        }

        // Inicializar el launcher para seleccionar archivo
        filePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri: Uri? = result.data?.data
                    uri?.let {
                        if (uri != null) {
                            val user = FirebaseAuth.getInstance().currentUser
                            Log.d(
                                "FirebaseUID",
                                "UID actual: ${user?.uid}"
                            ) // Aquí imprimimos el UID

                            //cada archivo subido tendrá un nombre diferente, aunque el contenido sea el mismo y no se sobreescribe ningún archivo en Firebase Storage
                            val nombreArchivo = "temario_${System.currentTimeMillis()}.pdf"
                            //val nombreArchivo = "temario_prueba_2.pdf"

                            FirebaseStorageHelper.subirArchivo(
                                requireContext(),
                                it,
                                nombreArchivo,
                                onSuccess = { url ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Archivo subido: $url",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                onError = {
                                    Toast.makeText(
                                        requireContext(),
                                        "Error al subir archivo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }
    }
}
