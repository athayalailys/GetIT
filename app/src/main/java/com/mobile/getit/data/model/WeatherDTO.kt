package com.mobile.getit.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponseDto(
    @SerializedName("current_weather") val currentWeather: CurrentWeatherDto
)

data class CurrentWeatherDto(
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("weathercode") val weatherCode: Int
)