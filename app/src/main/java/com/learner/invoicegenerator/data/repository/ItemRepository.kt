package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.ApiCalling.RetrofitInstance
import com.learner.invoicegenerator.ApiCalling.upcItem
import com.learner.invoicegenerator.data.local.Dao.ItemDao
import com.learner.invoicegenerator.data.local.entity.Item
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit

class ItemRepository(private val itemDao: ItemDao) {
    suspend fun insertItem(item: Item):Long  {
        return itemDao.insertItem(item)
    }
    suspend fun updateItem(item: Item) = itemDao.updateItem(item)
    suspend fun deleteItem(item: Item) = itemDao.deleteItem(item)
    fun getAllItems(workspaceId: Int): Flow<List<Item>> = itemDao.getAllItemsOfWorkspace(workspaceId)
    suspend fun getItemById(id: Int): Item? = itemDao.getItemById(id)
    fun searchItems(query: String, workspaceId: Int): Flow<List<Item>> = itemDao.searchItemsByName(query, workspaceId)


    suspend fun searchItemlocallyByBarcode(code:String,workspaceId: Int):Item?{
        return itemDao.searchItemByBarcode(code,workspaceId)
    }
    suspend fun getItemsDetail(code:String,workspaceId: Int): upcItem? {
        val localItem= itemDao.searchItemByBarcode(code,workspaceId)
        if(localItem!=null){
            return upcItem(
                    ean=localItem.barcode?:"",
                    title=localItem.itemName,
                     lowest_recorded_price = localItem.price
                    )

        }
        return(
                try{
                    val itemDetails= RetrofitInstance.api.getitemDetails(code)
                    itemDetails.items.firstOrNull()
                } catch (e: Exception){
                    null
                }
                )

    }
}