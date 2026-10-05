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

    @Query("SELECT * FROM Invoices WHERE workspaceId=:workspaceId ORDER BY issueDate DESC")
    fun getInvoicesByWorkspaceId(workspaceId:Int): Flow<List<Invoice>>

    @Query("SELECT * FROM Invoices WHERE invoiceNum=:invoiceNum")
    suspend fun getInvoiceByInvoiceNum(invoiceNum:String):Invoice?


    @Query("SELECT * From Invoices WHERE workspaceId=:workspaceId ORDER BY id DESC LIMIT 1 ")
    suspend fun getLatestInvoice(workspaceId: Int):Invoice?

    @Query("SELECT * FROM Invoices WHERE workspaceId=:workspaceId AND status='Paid' AND ((paidDate>=:startOfMonth AND paidDate<:startOfNextMonth) OR (issueDate>=:startOfMonth AND issueDate<:startOfNextMonth)) ORDER BY issueDate DESC")
    suspend fun getPaidInvoicesThisMonth(startOfMonth: LocalDate,startOfNextMonth: LocalDate,workspaceId: Int): List<Invoice>

    @Query("SELECT * FROM Invoices WHERE workspaceId=:workspaceId AND status!='Paid' AND dueDate<:today ORDER BY issueDate DESC")
    suspend fun getUnpaidInvoices(today: LocalDate,workspaceId: Int):List<Invoice>

    @Query("SELECT * FROM Invoices WHERE workspaceId=:workspaceId AND status!='Paid' AND dueDate>=:today ORDER BY issueDate DESC")
    suspend fun getPendingInvoicesThisMonth(today: LocalDate,workspaceId: Int):List<Invoice>

    @Query("SELECT * FROM Invoices WHERE workspaceId=:workspaceId AND (clientBusinessName LIKE '%' || :query || '%' OR SUBSTR(invoiceNum,-4) LIKE '%' || :query) ORDER BY issueDate DESC")
    fun searchInvoices(workspaceId:Int,query:String): Flow<List<Invoice>>

    @Query("""
    SELECT * FROM Invoices 
    WHERE workspaceId = :workspaceId 
      AND (
          (issueDate BETWEEN :fromDate AND :toDate)
          OR (status = 'Paid' AND paidDate BETWEEN :fromDate AND :toDate)
          OR (status != 'Paid')
      )
      AND (
          clientBusinessName LIKE '%' || :query || '%' 
          OR SUBSTR(invoiceNum, -4) LIKE '%' || :query
      )
    ORDER BY issueDate DESC
""")
    fun searchInvoicesInRange(
        workspaceId: Int,
        query: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Flow<List<Invoice>>

    @Query("""
    SELECT * FROM Invoices 
    WHERE workspaceId = :workspaceId 
      AND (
          (issueDate BETWEEN :fromDate AND :toDate)
          OR (status = 'Paid' AND paidDate BETWEEN :fromDate AND :toDate)
          OR (status != 'Paid')
      )
    ORDER BY issueDate DESC
""")
    fun getInvoicesByDateRange(workspaceId: Int, fromDate: LocalDate, toDate: LocalDate): Flow<List<Invoice>>



}