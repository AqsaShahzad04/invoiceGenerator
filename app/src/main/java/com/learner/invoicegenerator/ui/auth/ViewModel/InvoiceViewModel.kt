package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import com.learner.invoicegenerator.data.local.entity.NumberingReset
import com.learner.invoicegenerator.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repository: InvoiceRepository): ViewModel(){

    private val _addInvoiceState= MutableLiveData<InvoiceState>(InvoiceState.Idle)
    val addInvoiceState: LiveData<InvoiceState> get()=_addInvoiceState

    private val _selectedItems= MutableStateFlow<List<InvoiceItemLine>>(emptyList())
    val selectedItems: StateFlow<List<InvoiceItemLine>> =_selectedItems

    private val _invoiceDraft=MutableStateFlow<Invoice?>(null)
    val invoiceDraft: StateFlow<Invoice?> = _invoiceDraft

    fun updateInvoiceDraft(draft:Invoice){
        _invoiceDraft.value=draft

    }
    fun resetInvoiceDraft(){
        _invoiceDraft.value=null
    }
    fun resetState(){
        _addInvoiceState.value= InvoiceState.Idle
    }


    fun addToSelectedItems(item:InvoiceItemLine){
        _selectedItems.value = _selectedItems.value + item
    }
    fun updateSelectedItems(items: MutableList<InvoiceItemLine>){
        _selectedItems.value=items
    }
    fun removeFromSelectedItems(id:Int){
        _selectedItems.value=_selectedItems.value.filter{it.itemId!=id}
    }
    fun resetSelectedItems(){
        _selectedItems.value=emptyList()
    }

    fun incrementQuantity(id:Int){
        _selectedItems.value=_selectedItems.value.map{item->
            if(item.itemId==id){
                item.copy(itemQuantity = item.itemQuantity+1)
            }
            else{
                item
            }
        }
    }
    fun decrementQuantity(id:Int){
        _selectedItems.value=_selectedItems.value.map{item->
            if(item.itemId==id && item.itemQuantity>1){
                item.copy(itemQuantity = item.itemQuantity-1)
            }
            else{
                item
            }
        }
    }

      suspend fun getInvoiceNum(workspaceId: Int,resetPeriod: NumberingReset):Int{
        return repository.getInvoiceNum(workspaceId,resetPeriod)
    }
    fun insertInvoice(invoice: Invoice) {
        _addInvoiceState.value = InvoiceState.Loading
        viewModelScope.launch {
            try {
               val invoiceId= repository.insertInvoice(invoice)
                _addInvoiceState.value = InvoiceState.Success(invoiceId.toInt())
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
                    _addInvoiceState.value = InvoiceState.Success(invoice.id)
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
                        _addInvoiceState.value = InvoiceState.Success(invoice.id)
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
                val id=repository.insertInvoiceItemLine(item)
                _addInvoiceState.value = InvoiceState.Success(id.toInt())
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
                    _addInvoiceState.value = InvoiceState.Success(item.id)
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
                        _addInvoiceState.value = InvoiceState.Success(item.id)
                    } catch (e: Exception) {
                        _addInvoiceState.value = InvoiceState.Error(e.message ?: "Error occured")
                    }
                }
            }



    suspend fun getItemsbyInvoiceId(invoiceId:Int): Flow<List<InvoiceItemLine>>{
        return repository.getItemsfrominvoiceId(invoiceId)
    }


}