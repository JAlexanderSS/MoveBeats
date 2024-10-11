package com.example.movebeats

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Aquí puedes manejar las notificaciones cuando se publican
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Aquí puedes manejar las notificaciones cuando se eliminan
    }
}
