package com.learner.invoicegenerator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room entity to store in-app notifications
@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val invoiceId: Int? = null,
    val type: String = "GENERAL"
)
