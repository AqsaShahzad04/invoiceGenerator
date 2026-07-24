package com.learner.invoicegenerator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Clients")
data class Client (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val businessName: String,
    val contactPerson:String?,
    val email: String?,
    val phone: String?,
    val address: String?
)

