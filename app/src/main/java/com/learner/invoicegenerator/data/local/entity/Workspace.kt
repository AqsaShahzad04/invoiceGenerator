package com.learner.invoicegenerator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Workspaces")
data class Workspace(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val ownerUserId: Int,  // This links the workspace to a specific User
    val isDefault: Boolean = false, // Useful for multi-workspace support later
    val email: String? = null,
    val phone: String? = null,
    val taxNumber: String? = null,
    val address: String? = null,
    val logoUri: String? = null
)