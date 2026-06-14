package com.mobile.getit.data.remote

import com.mobile.getit.data.model.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") lat: Double = -3.44,
        @Query("longitude") lon: Double = 114.83,
        @Query("current_weather") current: Boolean = true
    ): WeatherResponseDto
}