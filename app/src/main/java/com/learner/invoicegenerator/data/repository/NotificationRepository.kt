package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.Dao.NotificationDao
import com.learner.invoicegenerator.data.local.entity.AppNotification
import kotlinx.coroutines.flow.Flow

// Repository to interface between NotificationDao and ViewModel/Workers
class NotificationRepository(private val dao: NotificationDao) {

    val allNotifications: Flow<List<AppNotification>> = dao.getAllNotifications()
    val unreadCount: Flow<Int> = dao.getUnreadCount()

    suspend fun insertNotification(notification: AppNotification): Long {
        return dao.insertNotification(notification)
    }

    suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    suspend fun clearAllNotifications() {
        dao.clearAllNotifications()
    }

    suspend fun getCountByType(type: String): Int {
        return dao.getCountByType(type)
    }

    suspend fun getCountByInvoiceAndType(invoiceId: Int, type: String): Int {
        return dao.getCountByInvoiceAndType(invoiceId, type)
    }
}
