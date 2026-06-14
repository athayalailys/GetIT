package com.mobile.getit.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobile.getit.data.local.AppDatabase
import com.mobile.getit.data.local.PreferenceManager
import com.mobile.getit.data.repository.GetItRepositoryImpl
import com.mobile.getit.data.remote.WeatherApiService
import com.mobile.getit.data.remote.ProductApiService
import com.mobile.getit.data.remote.FirebaseApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val retrofitWeather = Retrofit.Builder().baseUrl("https://api.open-meteo.com/").addConverterFactory(GsonConverterFactory.create()).build()
    private val retrofitDummy = Retrofit.Builder().baseUrl("https://dummyjson.com/").addConverterFactory(GsonConverterFactory.create()).build()
    private val retrofitFirebase = Retrofit.Builder().baseUrl("https://getit-182e0-default-rtdb.asia-southeast1.firebasedatabase.app/").addConverterFactory(GsonConverterFactory.create()).build()

    private val prefs = PreferenceManager(context)
    private val repo = GetItRepositoryImpl(
        AppDatabase.getDatabase(context).wishlistDao(),
        retrofitWeather.create(WeatherApiService::class.java),
        retrofitDummy.create(ProductApiService::class.java),
        retrofitFirebase.create(FirebaseApiService::class.java),
        prefs
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(WeatherViewModel::class.java) -> WeatherViewModel(repo) as T
            modelClass.isAssignableFrom(ProductViewModel::class.java) -> {
                val application = context.applicationContext as Application
                ProductViewModel(repo, application) as T
            }
            modelClass.isAssignableFrom(WishlistViewModel::class.java) -> WishlistViewModel(repo) as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repo) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repo) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(repo) as T
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> NotificationViewModel(repo) as T
            modelClass.isAssignableFrom(CategoryViewModel::class.java) -> CategoryViewModel(repo, "Semua", "empty") as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel Class: ${modelClass.name}")
        }
    }
}
