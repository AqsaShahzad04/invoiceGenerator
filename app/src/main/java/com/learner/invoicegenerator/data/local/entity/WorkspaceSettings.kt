package com.learner.invoicegenerator.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NumberingReset{
    YEARLY,
    MONTHLY,
    NEVER
}

enum class PaymentMethods{
    BANKTransfer,
    EasyPaisa,
    Jazzcash,
    Cash,
    Card,
    Cheque,
    USTD
}

enum class PaymentDueDateOffset{
    NET7,
    NET14,
    NET30,
    NET45,
    NET60
}

@Entity(tableName = "Settings",
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workspaceId"], unique = true)]
)
data class WorkspaceSettings (
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val workspaceId:Int,
    val invoicePrefix:String="INV-2026-",
    val numberingReset: NumberingReset= NumberingReset.YEARLY,
    val paymentDueDateOffset: PaymentDueDateOffset = PaymentDueDateOffset.NET14,
    val defaultTax: Boolean=false,
    val taxRate:Double=25.0,
    val lateFee: Double=1.5,
    val paymentMethods:List<PaymentMethods> = listOf(PaymentMethods.Cash, PaymentMethods.BANKTransfer),
    val discountLine: Boolean=false,
    val signatureBlock: Boolean=false,
    val defaultNotes:String?=null,
    val autoReminders: Boolean=false,
    val sendReminderAfterDueDays:Int=5,
    val notifications:Boolean=true
    )

