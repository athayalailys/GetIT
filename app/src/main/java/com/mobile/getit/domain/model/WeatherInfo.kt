package com.mobile.getit.domain.model

data class WeatherInfo(
    val temperature: Double,
    val weatherConditionResId: Int, // Menggunakan Resource ID agar bisa diterjemahkan
    val isSafeForCOD: Boolean
)