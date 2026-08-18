package com.learner.invoicegenerator.ui.clients.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.repository.ClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ClientViewModel(private val repository: ClientRepository) : ViewModel() {

    private val _addClientState = MutableLiveData<ClientState>()
    val addClientState: LiveData<ClientState> get() = _addClientState

    private val workspaceIdFlow = MutableStateFlow(1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allClients: Flow<List<Client>> = workspaceIdFlow.flatMapLatest { id ->
        repository.getAllClients(id)
    }

    fun setWorkspaceId(id: Int) {
        workspaceIdFlow.value = id
    }

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

    private val _updateState = MutableStateFlow<ClientState>(ClientState.Idle)
    val updateState: StateFlow<ClientState> = _updateState

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

    suspend fun getClientById(id:Int):Client?{
        return repository.getClientById(id)
    }
}
