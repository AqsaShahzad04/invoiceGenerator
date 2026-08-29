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
    suspend fun insertClient(client: Client):Long

    @Query("SELECT * FROM Clients WHERE workspaceId = :workspaceId")
     fun getAllClientsOfWorkspace(workspaceId:Int): Flow<List<Client>>

    @Query("SELECT * FROM Clients WHERE id = :id")
    suspend fun getClientById(id: Int): Client?

    @Delete
    suspend fun deleteClient(client: Client)

    @Update
    suspend fun updateClient(client: Client)


    @Query("SELECT * FROM Clients WHERE businessName LIKE '%' || :query || '%'")
     fun searchClientsByBusinessName(query: String): Flow<List<Client>>

    @Query("SELECT * FROM Clients WHERE contactPerson LIKE '%' || :query || '%'" )
     fun searchClientBYName(query:String):Flow<List<Client>>

}

