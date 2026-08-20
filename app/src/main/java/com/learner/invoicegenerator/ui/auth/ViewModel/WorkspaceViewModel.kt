package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.data.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WorkspaceViewModel(
    private val repository: WorkspaceRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _workspaceState = MutableLiveData<WorkspaceState>(WorkspaceState.Idle)
    val workspaceState: MutableLiveData<WorkspaceState> get() = _workspaceState

    fun resetState() {
        _workspaceState.value = WorkspaceState.Idle
    }

    fun addWorkspace(workspace: Workspace) {
        viewModelScope.launch {
            _workspaceState.value = WorkspaceState.Loading
            try {
                repository.insertWorkspace(workspace)
                _workspaceState.value = WorkspaceState.Success
            } catch (e: Exception) {
                _workspaceState.value = WorkspaceState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateWorkspace(workspace: Workspace) {
        viewModelScope.launch {
            _workspaceState.value = WorkspaceState.Loading
            try {
                repository.updateWorkspace(workspace)
                _workspaceState.value = WorkspaceState.Success
            } catch (e: Exception) {
                _workspaceState.value = WorkspaceState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteWorkspace(workspace: Workspace) {
        viewModelScope.launch {
            _workspaceState.value = WorkspaceState.Loading
            try {
                repository.deleteWorkspace(workspace)
                _workspaceState.value = WorkspaceState.Success
            } catch (e: Exception) {
                _workspaceState.value = WorkspaceState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getWorkspacesByUserId(userId: Int): Flow<List<Workspace>> = repository.getWorkspacesByUserId(userId)
}