package com.learner.invoicegenerator.ui.auth.ViewModel

sealed class WorkspaceSettingsState {
    object Idle: WorkspaceSettingsState()
    object Success: WorkspaceSettingsState()
    object Loading: WorkspaceSettingsState()
    class Error(message:String): WorkspaceSettingsState()
}