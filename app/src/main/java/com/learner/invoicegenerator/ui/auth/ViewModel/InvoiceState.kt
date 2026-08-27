package com.learner.invoicegenerator.ui.auth.ViewModel

sealed class  InvoiceState {

    object Idle: InvoiceState()
    object Loading: InvoiceState()
    object Success: InvoiceState()
    data class Error(val msg:String): InvoiceState()
}