package com.learner.invoicegenerator.data.local.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.learner.invoicegenerator.data.local.entity.Item
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemDao {
    @Insert
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("SELECT * FROM Items WHERE WorkspaceId=:workspaceId")
      fun getAllItemsOfWorkspace(workspaceId:Int): Flow<List<Item>>

    @Query("SELECT * FROM Items WHERE id=:id")
    suspend fun getItemById(id:Int):Item?

    @Query("SELECT * FROM Items WHERE itemName LIKE '%' || :query || '%' AND workspaceId=:workspaceId")
    fun searchItemsByName(query: String,workspaceId:Int): Flow<List<Item>>
}