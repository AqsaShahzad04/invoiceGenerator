package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.repository.WorkspaceRepository
import com.learner.invoicegenerator.data.repository.WorkspaceSettingsRepository

class WorkspaceViewModelFactory(
    private val repository: WorkspaceRepository,
    private val sessionManager: SessionManager,
    private val workspaceSettingsRepository: WorkspaceSettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkspaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkspaceViewModel(repository, sessionManager, workspaceSettingsRepository ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}