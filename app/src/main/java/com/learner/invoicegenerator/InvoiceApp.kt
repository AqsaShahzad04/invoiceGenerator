package com.learner.invoicegenerator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.utils.NotificationHelper
import com.learner.invoicegenerator.worker.NotificationWorker
import java.util.concurrent.TimeUnit

class InvoiceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val sessionManager = SessionManager.getInstance(this)
        if (sessionManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Initialize system notification channels
        NotificationHelper.createNotificationChannels(this)

        // Schedule daily periodic worker for overdue reminders & new month notifications
        scheduleDailyNotificationWorker()

        // Trigger welcome notification on first launch
        triggerWelcomeNotificationIfNeeded(sessionManager)
    }

    private fun scheduleDailyNotificationWorker() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyNotificationWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun triggerWelcomeNotificationIfNeeded(sessionManager: SessionManager) {
        if (!sessionManager.isWelcomeNotificationSent()) {
            NotificationHelper.showNotification(
                context = this,
                title = "Welcome to Ledger",
                message = "Add a client and an item, then send your first invoice.",
                type = "WELCOME"
            )
            sessionManager.setWelcomeNotificationSent()
        }
    }
}
