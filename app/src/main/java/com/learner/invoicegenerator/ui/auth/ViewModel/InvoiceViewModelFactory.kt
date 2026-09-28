package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.repository.InvoiceRepository

class InvoiceViewModelFactory(val repository: InvoiceRepository,private val sessionManager: SessionManager): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InvoiceViewModel(repository,sessionManager) as T
    }
}