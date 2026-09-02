package com.learner.invoicegenerator.data.local.Dao

import android.icu.text.MessagePattern
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.learner.invoicegenerator.data.local.entity.Workspace
import kotlinx.coroutines.flow.Flow


@Dao
interface WorkspaceDao {
    @Insert
    suspend fun insert(workspace: Workspace):Long

    @Query("SELECT * FROM Workspaces WHERE ownerUserId = :id")
     fun getWorkspaceByUserId(id: Int): Flow<List<Workspace>>

     @Query("SELECT * FROM Workspaces WHERE id = :id")
     suspend fun getWorkspaceById(id: Int): Workspace?

    @Update
    suspend fun Update(workspace: Workspace)

    @Delete
    suspend fun Delete(workspace: Workspace)
}