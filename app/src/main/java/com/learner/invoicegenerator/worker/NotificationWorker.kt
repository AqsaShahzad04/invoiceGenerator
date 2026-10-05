package com.learner.invoicegenerator.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.utils.NotificationHelper
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val sessionManager = SessionManager.getInstance(context)
        val db = DatabaseProvider.getDatabase(context)

        runBlocking {
            checkNewMonthNotification(sessionManager)
            if (sessionManager.isAutoRemindersEnabled()) {
                checkOverdueInvoices(sessionManager, db)
            }
        }

        return Result.success()
    }

    private fun checkNewMonthNotification(sessionManager: SessionManager) {
        val today = LocalDate.now()
        val currentYearMonth = "${today.year}-${today.monthValue}"

        // Check if today is 1st of month and notification has not been sent for this month
        if (today.dayOfMonth == 1 && sessionManager.getLastNewMonthNotification() != currentYearMonth) {
            val title = "Happy New Month!"
            val message = "May this new month bring peace, prosperity, and great success to your business!"
            NotificationHelper.showNotification(context, title, message, type = "NEW_MONTH")
            sessionManager.setLastNewMonthNotification(currentYearMonth)
        }
    }

    private suspend fun checkOverdueInvoices(
        sessionManager: SessionManager,
        db: com.learner.invoicegenerator.data.local.InvoiceDatabase
    ) {
        val today = LocalDate.now()
        val sendAfterDays = sessionManager.getReminderDays()
        val dao = db.invoiceDao()
        val notifDao = db.notificationDao()

        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()
        if (activeWorkspaceId <= 0) return

        // Get unpaid / pending invoices
        val unpaidInvoices = dao.getUnpaidInvoices(today.plusDays(1), activeWorkspaceId)

        for (invoice in unpaidInvoices) {
            if (invoice.status == "Paid") continue

            val overdueDays = ChronoUnit.DAYS.between(invoice.dueDate, today)

            // Check if today >= dueDate + sendAfterDays
            if (overdueDays >= sendAfterDays) {
                // Check if we already reminded for this invoice
                val count = notifDao.getCountByInvoiceAndType(invoice.id, "OVERDUE")
                if (count == 0) {
                    val title = "Overdue Invoice Reminder"
                    val clientName = if (invoice.clientBusinessName.isNotEmpty()) invoice.clientBusinessName else "Client"
                    val message = "Invoice #${invoice.invoiceNum} for $clientName is $overdueDays days overdue."

                    NotificationHelper.showNotification(
                        context = context,
                        title = title,
                        message = message,
                        type = "OVERDUE",
                        invoiceId = invoice.id
                    )
                }
            }
        }
    }
}
