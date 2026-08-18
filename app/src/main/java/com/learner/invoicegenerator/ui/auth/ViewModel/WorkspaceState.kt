package com.learner.invoicegenerator.ui.auth.ViewModel

sealed class WorkspaceState {
    object Idle : WorkspaceState()
    object Loading : WorkspaceState()
    object Success : WorkspaceState()
    data class Error(val message: String) : WorkspaceState()
}
