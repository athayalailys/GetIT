package com.mobile.getit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mobile.getit.R
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.ui.viewmodel.AuthViewModel
import com.mobile.getit.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory(context) }
    val authVm: AuthViewModel = viewModel(factory = factory)
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        authVm.checkInitialAuthStatus()
        delay(2000)
        
        // PERBAIKAN Clean Architecture: Logika dipindah ke ViewModel (isUserLoggedIn)
        if (authVm.isUserLoggedIn()) {
            navController.navigate(Screens.Home.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        } else {
            navController.navigate(Screens.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GetItPurple),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.getit_splash),
            contentDescription = "Logo GetIT",
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
        )
    }
}
