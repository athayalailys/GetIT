package com.mobile.getit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mobile.getit.navigations.Screens

@Composable
fun GetItBottomBar(
    navController: NavController, 
    currentRoute: String,
    isVerified: Boolean = false // Passed from screen to maintain Clean Architecture
) {
    val context = LocalContext.current

    val navigateTo = { route: String ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp).background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigateTo(Screens.Home.route) }) {
                Icon(Icons.Default.Home, "Home", tint = if (currentRoute == Screens.Home.route) MaterialTheme.colorScheme.primary else Color.Gray)
            }
            IconButton(onClick = { navigateTo(Screens.Wishlist.route) }) {
                Icon(Icons.Default.ShoppingCart, "Wishlist", tint = if (currentRoute == Screens.Wishlist.route) MaterialTheme.colorScheme.primary else Color.Gray)
            }
            Spacer(modifier = Modifier.width(48.dp))
            IconButton(onClick = { navigateTo(Screens.ManageProduct.route) }) {
                Icon(Icons.AutoMirrored.Filled.ListAlt, "Manage", tint = if (currentRoute == Screens.ManageProduct.route) MaterialTheme.colorScheme.primary else Color.Gray)
            }
            IconButton(onClick = { navigateTo(Screens.Settings.route) }) {
                Icon(Icons.Default.Settings, "Settings", tint = if (currentRoute == Screens.Settings.route) MaterialTheme.colorScheme.primary else Color.Gray)
            }
        }
        Box(
            modifier = Modifier.offset(y = (-15).dp).size(64.dp).background(MaterialTheme.colorScheme.secondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { 
                if (isVerified) {
                    navController.navigate(Screens.AddProduct.route)
                } else {
                    Toast.makeText(context, "Verifikasi identitas Anda terlebih dahulu di Pengaturan!", Toast.LENGTH_LONG).show()
                }
            }) {
                Icon(Icons.Default.Add, "Jual", tint = Color.Black, modifier = Modifier.size(32.dp))
            }
        }
    }
}
