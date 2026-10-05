package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.entity.AppNotification
import com.learner.invoicegenerator.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    val allNotifications: Flow<List<AppNotification>> = repository.allNotifications
    val unreadCount: Flow<Int> = repository.unreadCount

    init {
        // Seed initial fake test notification if list is empty for testing UI
        viewModelScope.launch {
            val current = repository.allNotifications.first()
            if (current.isEmpty()) {
                repository.insertNotification(
                    AppNotification(
                        title = "Welcome to Ledger",
                        message = "Add a client and an item, then send your first invoice.",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = "WELCOME"
                    )
                )
            }
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun addTestNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.insertNotification(
                AppNotification(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
        }
    }
}

class NotificationViewModelFactory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
