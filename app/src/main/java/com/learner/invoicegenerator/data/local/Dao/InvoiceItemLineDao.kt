package com.learner.invoicegenerator.data.local.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceItemLineDao {

    @Insert
    suspend fun addItemInInvoice(item: InvoiceItemLine)

    @Update
    suspend fun updateItemInInvoice(item: InvoiceItemLine)

    @Delete
    suspend fun deleteItemFromInvoice(item: InvoiceItemLine)

    @Query("SELECT * FROM invoiceitemsline WHERE invoiceId=:invoiceId")
    fun getItemsByInvoiceId(invoiceId:Int): Flow<List<InvoiceItemLine>>
}