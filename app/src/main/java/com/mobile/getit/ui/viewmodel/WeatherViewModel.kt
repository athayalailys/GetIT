package com.mobile.getit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.getit.domain.model.WeatherInfo
import com.mobile.getit.domain.repository.GetItRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: GetItRepository) : ViewModel() {
    private val _weatherState = MutableStateFlow<WeatherInfo?>(null)
    val weatherState: StateFlow<WeatherInfo?> = _weatherState

    fun fetchWeather() {
        viewModelScope.launch {
            _weatherState.value = repository.getWeatherBanjarmasin()
        }
    }
}