
package com.example.navegacion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Guarda el token en la base de datos bajo el UID del usuario actual
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val ref = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("tokens").child(uid)
            ref.setValue(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val titulo = remoteMessage.data["title"] ?: "Título"
        val cuerpo = remoteMessage.data["body"] ?: "Mensaje"
        val timestamp = System.currentTimeMillis()

        val notificacion = mapOf(
            "titulo" to titulo,
            "cuerpo" to cuerpo,
            "timestamp" to timestamp
        )

        val ref = FirebaseDatabase.getInstance("https://proyectofinal-75067-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("notificaciones").child(uid)

        ref.push().setValue(notificacion)
    }


    private fun mostrarNotificacion(titulo: String?, mensaje: String?) {
        val canalId = "canal_notificaciones"
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.drawable.bell)
            .setContentTitle(titulo ?: "Nueva notificación")
            .setContentText(mensaje ?: "")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Canal de Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(canal)
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
