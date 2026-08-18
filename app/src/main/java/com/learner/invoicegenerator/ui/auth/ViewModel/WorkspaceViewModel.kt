package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.data.repository.WorkspaceRepository
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WorkspaceViewModel(private val repository: WorkspaceRepository) : ViewModel() {


    private val _workspaceState = MutableLiveData<WorkspaceState>()
    val workspaceState: MutableLiveData<WorkspaceState> get()=_workspaceState



    fun addWorkspace(workspace: Workspace) {
       viewModelScope.launch {
           try {
               repository.insertWorkspace(workspace)
               _workspaceState.value=WorkspaceState.Success
           }
           catch (e:Exception){
               _workspaceState.value= WorkspaceState.Error(e.message?:"Unknown error")
           }

       }
    }
    fun updateWorkspace(workspace: Workspace){
        viewModelScope.launch {
            try {
                repository.updateWorkspace(workspace)
                _workspaceState.value= WorkspaceState.Success
            }
            catch (e:Exception){
                _workspaceState.value= WorkspaceState.Error(e.message?:"Unknown error")
            }
        }
    }
    fun deleteWorkspace(workspace: Workspace){
        viewModelScope.launch {
            try {
                repository.deleteWorkspace(workspace)
                _workspaceState.value= WorkspaceState.Success
            }
            catch (e:Exception){
                _workspaceState.value= WorkspaceState.Error(e.message?:"Unknown error")
            }
        }
        }
    fun getWorkspacesByUserId(userId: Int): Flow<List<Workspace>> = repository.getWorkspacesByUserId(userId)
    }
