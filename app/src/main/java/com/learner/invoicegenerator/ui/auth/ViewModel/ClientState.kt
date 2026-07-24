package com.learner.invoicegenerator.ui.clients.viewmodel

sealed class ClientState {
    object Success : ClientState()
    data class Error(val message: String) : ClientState()
}