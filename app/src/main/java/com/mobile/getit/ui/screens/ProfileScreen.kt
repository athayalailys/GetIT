package com.mobile.getit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.getit.R
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.ui.viewmodel.ProductViewModel
import com.mobile.getit.utils.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, sellerId: String, productVm: ProductViewModel) {
    val products by productVm.products.collectAsStateWithLifecycle()
    val currentUser by productVm.currentUser.collectAsStateWithLifecycle()

    val isMyProfile = remember(sellerId, currentUser) {
        sellerId == "current_user" || sellerId == currentUser?.username
    }
    
    val sellerProduct = remember(products, sellerId) {
        products.find { it.sellerId == sellerId }
    }
    
    val namaTampilan = if (isMyProfile) currentUser?.fullName ?: "User GetIT" else {
        sellerProduct?.sellerName ?: "Verified Seller"
    }
    
    val lokasiTampilan = if (isMyProfile) currentUser?.location ?: "Banjarmasin" else (sellerProduct?.sellerLocation ?: "Banjarmasin")
    val fotoTampilan = if (isMyProfile) currentUser?.photoUri ?: "" else (sellerProduct?.sellerPhotoUri ?: "")

    val targetId = if (isMyProfile) currentUser?.username ?: "" else sellerId
    val daganganPenjual = remember(products, targetId) {
        products.filter { it.sellerId == targetId }
    }
    val totalTerjual = remember(daganganPenjual) {
        daganganPenjual.count { it.isSold }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(isMyProfile) stringResource(R.string.profile_title) else stringResource(R.string.profile_seller_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isMyProfile) {
                        IconButton(onClick = { navController.navigate(Screens.Settings.route) }) {
                            Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GetItPurple)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(GetItPurple))
                
                Column(
                    modifier = Modifier.fillMaxWidth().offset(y = (-50).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 4.dp
                    ) {
                        if (fotoTampilan.isNotEmpty()) {
                            AsyncImage(
                                model = fotoTampilan,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = GetItPurple)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = namaTampilan,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = lokasiTampilan, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-30).dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(label = stringResource(R.string.profile_products), value = daganganPenjual.size.toString())
                    StatBox(label = stringResource(R.string.profile_sales), value = totalTerjual.toString())
                }
            }

            item {
                Text(
                    text = stringResource(R.string.profile_list_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            if (daganganPenjual.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.profile_no_items), color = Color.Gray)
                    }
                }
            }

            items(daganganPenjual) { prod ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { navController.navigate(Screens.Detail.createRoute(prod.id)) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(64.dp), 
                            shape = RoundedCornerShape(12.dp), 
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            AsyncImage(
                                model = prod.getDisplayImage(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = formatRupiah(prod.price), style = MaterialTheme.typography.bodyMedium, color = GetItPurple, fontWeight = FontWeight.Bold)
                            if (prod.isSold) {
                                Text(stringResource(R.string.profile_sold_out), style = MaterialTheme.typography.labelSmall, color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = GetItPurple)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
