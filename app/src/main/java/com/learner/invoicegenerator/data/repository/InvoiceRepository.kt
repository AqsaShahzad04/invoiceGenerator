package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.Dao.InvoiceDao
import com.learner.invoicegenerator.data.local.Dao.InvoiceItemLineDao
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val invoiceDao: InvoiceDao,private val invoiceItemLineDao: InvoiceItemLineDao){

    suspend fun insertInvoice(invoice: Invoice){
        invoiceDao.insertInvoice(invoice)
    }
    suspend fun updateInvoice(invoice: Invoice){
        invoiceDao.updateInvoice(invoice)
    }
    suspend fun deleteInvoice(invoice: Invoice){
        invoiceDao.deleteInvoice(invoice)
    }
    suspend fun getInvoiceByInvoiceNum(invoiceNum:String): Invoice?{
       return invoiceDao.getInvoiceByInvoiceNum(invoiceNum)

    }
    fun getInvoicesBYWorkspaceId(workspaceId:Int): Flow<List<Invoice>>{
        return invoiceDao.getInvoicesByWorkspaceId(workspaceId)
    }

    suspend fun insertInvoiceItemLine(item: InvoiceItemLine)=invoiceItemLineDao.addItemInInvoice(item)
    suspend fun updateInvoiceItemLine(item: InvoiceItemLine)=invoiceItemLineDao.updateItemInInvoice(item)
    suspend fun deleteInvoiceItemLine(item: InvoiceItemLine)=invoiceItemLineDao.deleteItemFromInvoice(item)
    fun getItemsfrominvoiceId(invoiceId:Int): Flow<List<InvoiceItemLine>> = invoiceItemLineDao.getItemsByInvoiceId(invoiceId)



}