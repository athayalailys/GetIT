package com.mobile.getit.data.model

import com.google.gson.annotations.SerializedName
import com.mobile.getit.data.local.WishlistEntity

data class ProductResponseDto(
    @SerializedName("products") val products: List<ProductDto>
)

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Int,
    @SerializedName("category") val category: String,
    @SerializedName("thumbnail") val thumbnail: String
)

fun ProductDto.toEntity(userId: String): WishlistEntity {
    return WishlistEntity(
        id = this.id.toString(), // Mengonversi Int ke String
        userId = userId,
        title = this.title,
        price = this.price.toDouble(), // Mengonversi Int ke Double
        description = this.description,
        thumbnail = this.thumbnail,
        notes = "" // Default kosong
    )
}