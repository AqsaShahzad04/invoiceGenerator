package com.learner.invoicegenerator.ui.auth.ViewModel

sealed class  InvoiceState {

    object Idle: InvoiceState()
    object Loading: InvoiceState()
    data class Success(val id:Int): InvoiceState()
    data class Error(val msg:String): InvoiceState()
}