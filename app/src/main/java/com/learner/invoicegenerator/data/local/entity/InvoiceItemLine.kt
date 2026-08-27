package com.learner.invoicegenerator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="InvoiceItemsLine")
data class InvoiceItemLine (
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val invoiceId:Int,
    val itemId:Int,
    val itemName:String,
    val unitPrice:Double,
    val itemQuantity:Double,
    val itemUnit:String
    )

