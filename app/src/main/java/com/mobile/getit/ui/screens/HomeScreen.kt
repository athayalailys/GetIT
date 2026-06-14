package com.mobile.getit.ui.screens

import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.getit.R
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.viewmodel.ProductViewModel
import com.mobile.getit.ui.viewmodel.WeatherViewModel
import com.mobile.getit.domain.model.getCategoryResId
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.utils.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, weatherVm: WeatherViewModel, productVm: ProductViewModel) {
    val context = LocalContext.current
    
    BackHandler {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                currentContext.finish()
                return@BackHandler
            }
            currentContext = currentContext.baseContext
        }
    }
    
    val weatherState by weatherVm.weatherState.collectAsStateWithLifecycle()
    val categories by productVm.categories.collectAsStateWithLifecycle()
    val bannerState by productVm.activeBanner.collectAsStateWithLifecycle()
    val bestOffers by productVm.bestOffers.collectAsStateWithLifecycle()
    val currentUser by productVm.currentUser.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        weatherVm.fetchWeather()
        productVm.loadMarketplaceProducts()
    }

    Scaffold(
        bottomBar = { 
            GetItBottomBar(
                navController = navController, 
                currentRoute = Screens.Home.route,
                isVerified = currentUser?.isVerified == true || currentUser?.email?.endsWith("@mhs.ulm.ac.id") == true || currentUser?.email?.endsWith("@ulm.ac.id") == true
            ) 
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
            // Header
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(bottom = 20.dp)) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.home_title), color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Color.White.copy(0.2f), CircleShape).clickable { navController.navigate(Screens.Notification.route) },
                                contentAlignment = Alignment.Center
                            ) { 
                                Icon(Icons.Default.Notifications, "Notif", tint = Color.White) 
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier.size(40.dp).background(Color.LightGray, CircleShape).clickable { 
                                    navController.navigate(Screens.Profile.createRoute("current_user")) 
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentUser?.photoUri?.isNotEmpty() == true) {
                                    AsyncImage(model = currentUser?.photoUri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Person, null, tint = Color.White)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        placeholder = { Text(stringResource(R.string.search_placeholder), color = Color.Gray, style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Search, "Cari", tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                navController.navigate(Screens.Category.createRoute("empty", searchQuery))
                            }
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                // Weather Widget
                item {
                    weatherState?.let {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(0.05f))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Banjarmasin: ${it.temperature}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(it.weatherConditionResId), style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.weight(1f))
                                    Text(
                                        text = if(it.isSafeForCOD) stringResource(R.string.weather_safe) else stringResource(R.string.weather_unsafe),
                                        style = MaterialTheme.typography.labelSmall, 
                                        fontWeight = FontWeight.ExtraBold, 
                                        color = if(it.isSafeForCOD) Color(0xFF2E7D32) else Color.Red
                                    )
                                }
                            }
                        }
                    }
                }

                // Banner Promo
                bannerState?.let { banner ->
                    item {
                        val bannerBgColor = remember(banner.backgroundColorHex) {
                            try { Color(android.graphics.Color.parseColor(banner.backgroundColorHex)) } catch (e: Exception) { GetItPurple }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(bannerBgColor, RoundedCornerShape(24.dp))) {
                            if (banner.imageUrl.isNotEmpty()) {
                                AsyncImage(model = banner.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(banner.tagline, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(banner.title, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }

                // Penawaran Terbaik
                if (bestOffers.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.best_offer), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(bestOffers) { product ->
                                Card(
                                    modifier = Modifier.width(220.dp).clickable { navController.navigate(Screens.Detail.createRoute(product.id)) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        AsyncImage(
                                            model = product.getDisplayImage(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        Text(
                                            text = stringResource(getCategoryResId(product.category)), 
                                            style = MaterialTheme.typography.labelSmall, 
                                            color = Color.Gray
                                        )

                                        Text(product.title, style = MaterialTheme.typography.titleMedium, color = GetItPurple, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(formatRupiah(product.price), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                                        if (product.isSold) {
                                            Text(stringResource(R.string.profile_sold_out), fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Kategori
                item {
                    Text(stringResource(R.string.categories), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        categories.chunked(2).forEach { chunks ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                for (cat in chunks) {
                                    Card(
                                        modifier = Modifier.weight(1f).height(54.dp).clickable { navController.navigate(Screens.Category.createRoute(cat.technicalName)) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(cat.iconEmoji, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(stringResource(cat.nameResId), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
