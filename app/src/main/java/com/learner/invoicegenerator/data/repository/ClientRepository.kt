package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.Dao.Clientdao
import com.learner.invoicegenerator.data.local.entity.Client
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val ClientDao: Clientdao) {
    suspend fun insertClient(client: Client) {
        ClientDao.insertClient(client)
    }
    fun getAllClients(): Flow<List<Client>> {
        return ClientDao.getAllClients()
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
    fun searchClientsByBusinessName(query: String): Flow<List<Client>> {
        return ClientDao.searchClientsByBusinessName(query)
    }
    fun searchClientBYName(query: String): Flow<List<Client>> {
        return ClientDao.searchClientBYName(query)
    }
}

