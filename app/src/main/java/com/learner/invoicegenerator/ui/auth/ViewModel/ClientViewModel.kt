package com.learner.invoicegenerator.ui.clients.viewmodel

import androidx.compose.ui.graphics.Path.Companion.combine
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.repository.ClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ClientViewModel(
    private val repository: ClientRepository,
    private val sessionManager: SessionManager
) : ViewModel() {



    private val searchQuery=MutableStateFlow("")


    fun setSearchQuery(query:String){
        searchQuery.value=query
    }
    private val _addClientState = MutableLiveData<ClientState>(ClientState.Idle)
    val addClientState: LiveData<ClientState> get() = _addClientState

    private var _selectedClient= MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient

    fun resetState() {
        _addClientState.value = ClientState.Idle
    }

    fun resetselectedClients(){
        _selectedClient.value=null
    }



    val allClients: Flow<List<Client>> = combine(sessionManager.activeWorkspaceId,searchQuery){id,query->
        Pair(id,query)
    }.flatMapLatest {(id,query)->
        (if(query.isEmpty()){
            repository.getAllClients(id)
        } else{
           repository.searchClients(id,query)
        }) as Flow<List<Client>>
    }
    fun selectClient(newClient:Client){
        _selectedClient.value = newClient
    }
    fun addClient(client: Client) {
        viewModelScope.launch {
            _addClientState.value = ClientState.Loading
            try {
                val generatedId = repository.insertClient(client)
                val savedClient = client.copy(id = generatedId.toInt())
                _selectedClient.value = savedClient
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
