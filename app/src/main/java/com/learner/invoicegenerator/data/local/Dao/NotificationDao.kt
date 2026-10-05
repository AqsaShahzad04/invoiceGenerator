package com.learner.invoicegenerator.data.local.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learner.invoicegenerator.data.local.entity.AppNotification
import kotlinx.coroutines.flow.Flow

// DAO interface for managing notification records in Room
@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("SELECT COUNT(*) FROM notifications WHERE type = :type")
    suspend fun getCountByType(type: String): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE invoiceId = :invoiceId AND type = :type")
    suspend fun getCountByInvoiceAndType(invoiceId: Int, type: String): Int
}
