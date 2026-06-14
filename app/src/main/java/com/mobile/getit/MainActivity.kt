package com.mobile.getit

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.getit.navigations.SetupNavGraph
import com.mobile.getit.ui.theme.GetITTheme
import com.mobile.getit.ui.viewmodel.MainViewModel
import com.mobile.getit.ui.viewmodel.ViewModelFactory
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        askNotificationPermission()

        setContent {
            val factory = ViewModelFactory(this)
            val mainVm: MainViewModel = viewModel(factory = factory)
            
            val isDarkMode by mainVm.isDarkMode.collectAsStateWithLifecycle()
            val language by mainVm.language.collectAsStateWithLifecycle()
            
            val locale = Locale(language)
            Locale.setDefault(locale)
            
            val configuration = Configuration(LocalContext.current.resources.configuration)
            configuration.setLocale(locale)
            
            val localizedContext = LocalContext.current.createConfigurationContext(configuration)

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                GetITTheme(darkTheme = isDarkMode) {
                    SetupNavGraph()
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
