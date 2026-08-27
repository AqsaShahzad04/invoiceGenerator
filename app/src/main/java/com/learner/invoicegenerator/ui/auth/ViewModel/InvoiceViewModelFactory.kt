package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.learner.invoicegenerator.data.repository.InvoiceRepository

class InvoiceViewModelFactory(val repository: InvoiceRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InvoiceViewModelFactory(repository) as T
    }
}