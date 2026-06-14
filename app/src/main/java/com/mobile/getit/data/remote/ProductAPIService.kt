package com.mobile.getit.data.remote

import com.mobile.getit.data.model.ProductResponseDto
import retrofit2.http.GET

interface ProductApiService {
    @GET("products/category/smartphones")
    suspend fun getSmartphones(): ProductResponseDto

    @GET("products/category/laptops")
    suspend fun getLaptops(): ProductResponseDto
}