package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.Dao.Clientdao
import com.learner.invoicegenerator.data.local.entity.Client
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val ClientDao: Clientdao) {
    suspend fun insertClient(client: Client,workspaceId: Int):Long {
        val finalClient=client.copy(workspaceId=workspaceId)
       return ClientDao.insertClient(finalClient)
    }
    fun getAllClients(workspaceId:Int): Flow<List<Client>> {
        return ClientDao.getAllClientsOfWorkspace(workspaceId)
    }

    suspend fun getClientsByWorkspaceId(workspaceId:Int): List<Client> {
        return ClientDao.getClientsByworkspaceId(workspaceId)
    }
    suspend fun getClientById(id: Int,workspaceId: Int): Client? {
        return ClientDao.getClientById(id,workspaceId)
    }
    suspend fun deleteClient(client: Client,workspaceId: Int) {
        val finalClient=client.copy(workspaceId=workspaceId)
        ClientDao.deleteClient(finalClient)
    }
    suspend fun updateClient(client: Client,workspaceId: Int) {
        val finalClient=client.copy(workspaceId=workspaceId)
        ClientDao.updateClient(finalClient)
    }
    fun searchClients(workspaceId: Int,query: String): Flow<List<Client>> {
        return ClientDao.searchClients(workspaceId,query)
    }

}

