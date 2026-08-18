package com.learner.invoicegenerator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName="Items")
data class Item (
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val itemName: String,
    val barcode: String?,
    val price: Double=0.0,
    val unit:String,
    val category:String,
    val workspaceId:Int = 0
    )