package com.learner.invoicegenerator.data.local.Dao

import android.R
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.google.android.gms.common.api.Status
import com.learner.invoicegenerator.data.local.entity.Invoice
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InvoiceDao {

    @Insert
    suspend fun insertInvoice(invoice: Invoice):Long

    @Update
    suspend fun updateInvoice(invoice:Invoice)

    @Delete
    suspend fun deleteInvoice(invoice:Invoice)

    @Query("SELECT * FROM Invoices WHERE workspaceId=:workspaceId")
    fun getInvoicesByWorkspaceId(workspaceId:Int): List<Invoice>

    @Query("SELECT * FROM Invoices WHERE invoiceNum=:invoiceNum")
    suspend fun getInvoiceByInvoiceNum(invoiceNum:String):Invoice?


    @Query("SELECT * From Invoices WHERE workspaceId=:workspaceId ORDER BY id DESC LIMIT 1 ")
    suspend fun getLatestInvoice(workspaceId: Int):Invoice?

    @Query("SELECT * FROM Invoices WHERE status='Paid' AND (paidDate>=:startOfMonth AND paidDate<:startOfNextMonth) AND workspaceId=:workspaceId")
    suspend fun getPaidInvoicesThisMonth(startOfMonth: LocalDate,startOfNextMonth: LocalDate,workspaceId: Int): List<Invoice>


    @Query("SELECT * FROM Invoices WHERE status='Pending' AND dueDate<:today AND workspaceId=:workspaceId")
    suspend fun getUnpaidInvoices(today: LocalDate,workspaceId: Int):List<Invoice>


    @Query("SELECT * FROM Invoices WHERE status='Pending' AND(dueDate>=:startOfMonth AND dueDate<:startOFNextMonth) AND workspaceId=:workspaceId")
    suspend fun getPendingInvoicesThisMonth(startOfMonth: LocalDate,startOFNextMonth: LocalDate,workspaceId: Int):List<Invoice>





}