package com.learner.invoicegenerator.ui.clients.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.repository.ClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ClientViewModel(
    private val repository: ClientRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _addClientState = MutableLiveData<ClientState>(ClientState.Idle)
    val addClientState: LiveData<ClientState> get() = _addClientState

    fun resetState() {
        _addClientState.value = ClientState.Idle
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allClients: Flow<List<Client>> = sessionManager.activeWorkspaceId.flatMapLatest { id ->
        repository.getAllClients(id)
    }

    fun addClient(client: Client) {
        viewModelScope.launch {
            _addClientState.value = ClientState.Loading
            try {
                repository.insertClient(client)
                _addClientState.value = ClientState.Success
            } catch (e: Exception) {
                _addClientState.value = ClientState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _updateState = MutableLiveData<ClientState>(ClientState.Idle)
    val updateState: LiveData<ClientState> = _updateState

    fun updateClient(client: Client) {
        viewModelScope.launch {
            try {
                repository.updateClient(client)
                _updateState.value = ClientState.Success
            } catch (e: Exception) {
                _updateState.value = ClientState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            _addClientState.value = ClientState.Loading
            try {
                repository.deleteClient(client)
                _addClientState.value = ClientState.Success
            } catch (e: Exception) {
                _addClientState.value = ClientState.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getClientById(id: Int): Client? {
        return repository.getClientById(id)
    }
}
