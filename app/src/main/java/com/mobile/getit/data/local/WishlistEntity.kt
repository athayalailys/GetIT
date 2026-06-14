package com.mobile.getit.data.local

import androidx.room.Entity

@Entity(
    tableName = "wishlist_table",
    primaryKeys = ["id", "userId"]
)
data class WishlistEntity(
    val id: String, 
    val userId: String,
    val title: String,
    val price: Double,
    val description: String? = null,
    val thumbnail: String? = null,
    val notes: String? = null
)
