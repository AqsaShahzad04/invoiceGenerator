package com.learner.invoicegenerator.data.local.entity

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName="Invoices")
data class Invoice (
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val invoiceNum:String,
    val workspaceId:Int,
    val clientId:Int,
    val status:String,
    val issueDate:Date,
    val dueDate:Date,
    val currencyCode:String,
    val taxPercentage:Double,
    val discountType:String,
    val discountValue:Double,
    val endNote:String,
    val createdAt:Date,
    val updatedAt:Date
)

