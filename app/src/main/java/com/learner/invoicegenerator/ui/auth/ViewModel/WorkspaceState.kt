package com.learner.invoicegenerator.ui.auth.ViewModel

sealed class WorkspaceState {
    object Idle : WorkspaceState()
    object Loading : WorkspaceState()
    class Success(val id:Int) : WorkspaceState()
    data class Error(val message: String) : WorkspaceState()
}
