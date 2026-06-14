package com.mobile.getit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.getit.data.local.NotificationData
import com.mobile.getit.domain.repository.GetItRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NotificationViewModel(private val repository: GetItRepository) : ViewModel() {
    
    // Menggunakan flow reaktif agar UI otomatis terupdate saat ada notifikasi baru masuk
    val notifications: StateFlow<List<NotificationData>> = repository.observeNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
