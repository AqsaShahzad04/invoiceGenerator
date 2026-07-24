package com.learner.invoicegenerator.ui.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.learner.invoicegenerator.data.repository.ClientRepository

class ClientViewModelFactory(private val repository: ClientRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClientViewModel(repository) as T
    }
}