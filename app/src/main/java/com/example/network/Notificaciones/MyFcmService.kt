package com.example.network.Notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.netwokr.Chat.ChatActivity
import com.example.network.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFcmService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Si hay datos en la notificación, mostramos la notificación
        if (remoteMessage.notification != null) {
            val senderUid = remoteMessage.data["senderUid"]

            // Llamamos a la función para mostrar la notificación
            showNotification(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body,
                senderUid ?: "Desconocido"
            )
        } else {
            Log.d("FCM", "Mensaje recibido sin notificación")
        }
    }

    private fun showNotification(title: String?, body: String?, senderUid: String) {
        // Usar el tiempo actual para generar un ID único
        val notificationId = System.currentTimeMillis().toInt()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        configurarCanalNotification(notificationManager)

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("uidVendedor", senderUid)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "default_channel")
            .setSmallIcon(R.drawable.icono_notificacion)
            .setContentTitle(title ?: "Sin título")
            .setContentText(body ?: "Sin contenido")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Mostrar la notificación
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun configurarCanalNotification(notificationManager: NotificationManager) {
        // Crear canal de notificación solo si el dispositivo tiene Android Oreo o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "default_channel",
                "Chat_Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Show Chat Notifications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
