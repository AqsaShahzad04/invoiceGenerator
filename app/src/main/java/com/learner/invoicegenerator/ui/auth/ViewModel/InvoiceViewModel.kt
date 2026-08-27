package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import com.learner.invoicegenerator.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repository: InvoiceRepository): ViewModel(){

    private val _addInvoiceState= MutableLiveData<InvoiceState>(InvoiceState.Idle)
    val addInvoiceState: LiveData<InvoiceState> get()=_addInvoiceState

    fun insertInvoice(invoice: Invoice) {
        _addInvoiceState.value = InvoiceState.Loading
        viewModelScope.launch {
            try {
                repository.insertInvoice(invoice)
                _addInvoiceState.value = InvoiceState.Success
            } catch (error: Exception) {
                _addInvoiceState.value = InvoiceState.Error(error.message ?: "invalid error")
            }

        }
    }

        fun updateInvoice(invoice: Invoice) {
            _addInvoiceState.value = InvoiceState.Loading
            viewModelScope.launch {
                try {
                    repository.updateInvoice(invoice)
                    _addInvoiceState.value = InvoiceState.Success
                } catch (error: Exception) {
                    _addInvoiceState.value = InvoiceState.Error(error.message ?: "invalid error")
                }

            }
        }
            fun deleteInvoice(invoice: Invoice) {
                _addInvoiceState.value = InvoiceState.Loading
                viewModelScope.launch {
                    try {
                        repository.deleteInvoice(invoice)
                        _addInvoiceState.value = InvoiceState.Success
                    } catch (error: Exception) {
                        _addInvoiceState.value =
                            InvoiceState.Error(error.message ?: "invalid error")
                    }

                }
            }

                suspend fun getInvoicesBYWorkspaceId(workspaceId:Int): Flow<List<Invoice>>{
                    return repository.getInvoicesBYWorkspaceId(workspaceId)
                }

                suspend fun getInvoiceByInvoiceNum(invoiceNum:String): Invoice?{
                     return repository.getInvoiceByInvoiceNum(invoiceNum)
    }

    fun insertInvoiceItemLine(item: InvoiceItemLine) {
        _addInvoiceState.value = InvoiceState.Loading
        viewModelScope.launch {
            try {
                repository.insertInvoiceItemLine(item)
                _addInvoiceState.value = InvoiceState.Success
            } catch (e: Exception) {
                _addInvoiceState.value = InvoiceState.Error(e.message ?: "Error occured")
            }
        }
    }
        fun updateInvoiceItemLine(item: InvoiceItemLine) {
            _addInvoiceState.value = InvoiceState.Loading
            viewModelScope.launch {
                try {
                    repository.updateInvoiceItemLine(item)
                    _addInvoiceState.value = InvoiceState.Success
                } catch (e: Exception) {
                    _addInvoiceState.value = InvoiceState.Error(e.message ?: "Error occured")
                }
            }
        }

            fun deleteInvoiceItemLine(item: InvoiceItemLine) {
                _addInvoiceState.value = InvoiceState.Loading
                viewModelScope.launch {
                    try {
                        repository.deleteInvoiceItemLine(item)
                        _addInvoiceState.value = InvoiceState.Success
                    } catch (e: Exception) {
                        _addInvoiceState.value = InvoiceState.Error(e.message ?: "Error occured")
                    }
                }
            }



    suspend fun getItemsbyInvoiceId(invoiceId:Int): Flow<List<InvoiceItemLine>>{
        return repository.getItemsfrominvoiceId(invoiceId)
    }


}