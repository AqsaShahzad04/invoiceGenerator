package com.learner.invoicegenerator.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.learner.invoicegenerator.MainActivity
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.local.entity.AppNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {

    const val CHANNEL_REMINDERS_ID = "channel_invoice_reminders"
    const val CHANNEL_GENERAL_ID = "channel_general"

    // Create system notification channels for Android O+
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Invoice Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for overdue and pending invoices"
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL_ID,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General updates and welcome messages"
            }

            manager.createNotificationChannel(remindersChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    // Saves notification to Room DB AND shows system notification bar banner
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        type: String = "GENERAL",
        invoiceId: Int? = null
    ) {
        val applicationContext = context.applicationContext

        // 1. Save to Room database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseProvider.getDatabase(applicationContext)
                db.notificationDao().insertNotification(
                    AppNotification(
                        title = title,
                        message = message,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        invoiceId = invoiceId,
                        type = type
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Show system notification
        val channelId = if (type == "OVERDUE") CHANNEL_REMINDERS_ID else CHANNEL_GENERAL_ID

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_DESTINATION", "NOTIFICATIONS")
            invoiceId?.let { putExtra("EXTRA_INVOICE_ID", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (e: SecurityException) {
            // Permission denied on Android 13+
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
