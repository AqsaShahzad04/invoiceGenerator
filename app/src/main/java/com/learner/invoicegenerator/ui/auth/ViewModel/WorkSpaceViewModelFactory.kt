package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.learner.invoicegenerator.data.repository.WorkspaceRepository

class WorkSpaceViewModelFactory(private val repository: WorkspaceRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkspaceViewModel(repository) as T
    }
}