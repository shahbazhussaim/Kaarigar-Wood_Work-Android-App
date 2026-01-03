package com.kaarigar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val basePrice: Double,
    val stock: Int,
    val imageUrl: String,
    // Store JSON string for variants as simple approach or rely on remote for complex details
    val variantsJson: String? = null 
)
