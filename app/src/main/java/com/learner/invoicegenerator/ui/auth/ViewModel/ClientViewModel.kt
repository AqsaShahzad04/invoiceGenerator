package com.learner.invoicegenerator.ui.clients.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ClientViewModel(private val repository: ClientRepository) : ViewModel() {

    private val _addClientState = MutableLiveData<ClientState>()
    val addClientState: LiveData<ClientState> get() = _addClientState

    val allClients: Flow<List<Client>> = repository.getAllClients()

    fun addClient(client: Client) {
        viewModelScope.launch {
            try {
                repository.insertClient(client)
                _addClientState.value = ClientState.Success
            } catch (e: Exception) {
                _addClientState.value = ClientState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            try {
                repository.updateClient(client)
                _addClientState.value = ClientState.Success
            } catch (e: Exception) {
                _addClientState.value = ClientState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            try {
                repository.deleteClient(client)
                _addClientState.value = ClientState.Success
            } catch (e: Exception) {
                _addClientState.value = ClientState.Error(e.message ?: "Unknown error")
            }
        }
    }
}