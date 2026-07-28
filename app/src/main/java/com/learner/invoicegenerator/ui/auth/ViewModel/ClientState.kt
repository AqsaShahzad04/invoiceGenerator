package com.learner.invoicegenerator.ui.clients.viewmodel

import com.learner.invoicegenerator.data.local.entity.Client

sealed class ClientState {
    object Loading : ClientState()
    object Success : ClientState()
    data class Error(val message: String) : ClientState()
}