package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.Dao.WorkspaceDao
import com.learner.invoicegenerator.data.local.entity.Workspace

class WorkspaceRepository(private val workspaceDao: WorkspaceDao)  {
    suspend fun insertWorkspace(workspace: Workspace):Long {
        return workspaceDao.insert(workspace)
    }
    suspend fun updateWorkspace(workspace: Workspace) {
        workspaceDao.Update(workspace)
    }
    suspend fun deleteWorkspace(workspace: Workspace) {
        workspaceDao.Delete(workspace)
    }
    fun getWorkspacesByUserId(userId: Int) = workspaceDao.getWorkspaceByUserId(userId)

    suspend fun getWorkspacebyId(id: Int) = workspaceDao.getWorkspaceById(id)
}