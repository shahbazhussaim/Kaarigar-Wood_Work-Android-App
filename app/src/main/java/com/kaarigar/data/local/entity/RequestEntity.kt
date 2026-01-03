package com.kaarigar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey
    val id: String,
    val customerId: String,
    val type: String, // "REPAIR", "CUSTOM"
    val status: String, // "PENDING", "ACCEPTED", "IN_PROGRESS", "COMPLETED"
    val description: String,
    val imageUrl: String?,
    val predictedPrice: Double?,
    val workerId: String? = null,
    val createdAt: Long
)
