package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.Dao.ItemDao
import com.learner.invoicegenerator.data.local.entity.Item
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {
    suspend fun insertItem(item: Item) = itemDao.insertItem(item)
    suspend fun updateItem(item: Item) = itemDao.updateItem(item)
    suspend fun deleteItem(item: Item) = itemDao.deleteItem(item)
    fun getAllItems(workspaceId: Int): Flow<List<Item>> = itemDao.getAllItemsOfWorkspace(workspaceId)
    suspend fun getItemById(id: Int): Item? = itemDao.getItemById(id)
    fun searchItems(query: String, workspaceId: Int): Flow<List<Item>> = itemDao.searchItemsByName(query, workspaceId)
}