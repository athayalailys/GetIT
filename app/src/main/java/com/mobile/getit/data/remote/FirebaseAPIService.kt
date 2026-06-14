package com.mobile.getit.data.remote

import com.mobile.getit.domain.model.Product
import retrofit2.http.*

interface FirebaseApiService {
    @GET("products.json")
    suspend fun getC2CProducts(): Map<String, Product>?

    @POST("products.json")
    suspend fun addC2CProduct(@Body product: Product): Any

    @PUT("products/{id}.json")
    suspend fun updateC2CProduct(@Path("id") id: String, @Body product: Product): Product

    @DELETE("products/{id}.json")
    suspend fun deleteC2CProduct(@Path("id") id: String): Any
}