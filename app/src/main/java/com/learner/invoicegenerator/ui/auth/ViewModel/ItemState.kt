package com.learner.invoicegenerator.ui.auth.ViewModel

sealed class ItemState {
    object Idle : ItemState()
    object Loading : ItemState()
    object Success : ItemState()
    data class Error(val message: String) : ItemState()
}
