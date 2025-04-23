package com.example.navegacion.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.navegacion.R

class SoporteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout fragment_soporte.xml
        return inflater.inflate(R.layout.fragment_soporte, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Capturamos el TextView del email y lanzamos un Intent de correo al pulsarlo
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        tvEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:soporte@tuapp.com")
                putExtra(Intent.EXTRA_SUBJECT, "Soporte App")
                // opcional: putExtra(Intent.EXTRA_TEXT, "Describe tu problema…")
            }
            startActivity(emailIntent)
        }
    }
}
