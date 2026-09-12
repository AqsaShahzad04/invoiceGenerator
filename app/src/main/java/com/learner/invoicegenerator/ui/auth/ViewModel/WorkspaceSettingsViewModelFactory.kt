package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.learner.invoicegenerator.data.repository.WorkspaceSettingsRepository

class WorkspaceSettingsViewModelFactory(private val repository: WorkspaceSettingsRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkspaceSettingsViewModel(repository) as T
    }


}