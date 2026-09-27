package com.learner.invoicegenerator.data.local.entity

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.Date

@Entity(tableName="Invoices")
data class Invoice (
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val invoiceNum:String,
    val workspaceId:Int,
    val clientId:Int,
    val clientBusinessName: String,
    val status:String,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val paidDate:LocalDate?=null,
    val currencyCode:String,
    val taxPercentage:Double,
    val discountType:String,
    val discountValue:Double,
    val signaturePath:String?=null,
    val endNote:String,
    val pdfPath:String?=null,
    val totalAmount: Double=0.0
)

