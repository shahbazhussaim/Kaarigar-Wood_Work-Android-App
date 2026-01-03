package com.kaarigar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val email: String,
    val role: String, // "CUSTOMER", "WORKER", "ADMIN"
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val cachedTimestamp: Long = System.currentTimeMillis()
)
