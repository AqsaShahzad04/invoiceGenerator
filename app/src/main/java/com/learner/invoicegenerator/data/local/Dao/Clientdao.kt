package com.learner.invoicegenerator.data.local.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.learner.invoicegenerator.data.local.entity.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface Clientdao {
    @Insert
    suspend fun insertClient(client: Client,workspaceId: Int):Long

    @Query("SELECT * FROM Clients WHERE workspaceId = :workspaceId")
     fun getAllClientsOfWorkspace(workspaceId:Int): Flow<List<Client>>

     @Query("SELECT * FROM Clients WHERE workspaceId= :workspaceId")
    suspend fun getClientsByworkspaceId(workspaceId: Int): List<Client>
    @Query("SELECT * FROM Clients WHERE id = :id AND workspaceId =:workspaceId")
    suspend fun getClientById(id: Int,workspaceId: Int): Client?

    @Delete
    suspend fun deleteClient(client: Client,workspaceId: Int)

    @Update
    suspend fun updateClient(client: Client,workspaceId: Int)

    @Query("""
    SELECT * FROM Clients 
    WHERE workspaceId = :workspaceId 
    AND (businessName LIKE  :query || '%' OR contactPerson LIKE  :query || '%')
""") fun searchClients(workspaceId: Int,query: String): Flow<List<Client>>



}

