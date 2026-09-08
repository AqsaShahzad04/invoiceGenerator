package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.Dao.Clientdao
import com.learner.invoicegenerator.data.local.entity.Client
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val ClientDao: Clientdao) {
    suspend fun insertClient(client: Client):Long {
       return ClientDao.insertClient(client)
    }
    fun getAllClients(workspaceId:Int): Flow<List<Client>> {
        return ClientDao.getAllClientsOfWorkspace(workspaceId)
    }
    suspend fun getClientById(id: Int): Client? {
        return ClientDao.getClientById(id)
    }
    suspend fun deleteClient(client: Client) {
        ClientDao.deleteClient(client)
    }
    suspend fun updateClient(client: Client) {
        ClientDao.updateClient(client)
    }
    fun searchClients(workspaceId: Int,query: String): Flow<List<Client>> {
        return ClientDao.searchClients(workspaceId,query)
    }

}

