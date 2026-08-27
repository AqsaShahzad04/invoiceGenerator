package com.learner.invoicegenerator.data.local.Dao

import android.R
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.learner.invoicegenerator.data.local.entity.Invoice
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Insert
    suspend fun insertInvoice(invoice: Invoice)

    @Update
    suspend fun updateInvoice(invoice:Invoice)

    @Delete
    suspend fun deleteInvoice(invoice:Invoice)

    @Query("SELECT * FROM Invoices WHERE workspaceId=:workspaceId")
    fun getInvoicesByWorkspaceId(workspaceId:Int): Flow<List<Invoice>>

    @Query("SELECT * FROM Invoices WHERE invoiceNum=:invoiceNum")
    suspend fun getInvoiceByInvoiceNum(invoiceNum:String):Invoice?



}