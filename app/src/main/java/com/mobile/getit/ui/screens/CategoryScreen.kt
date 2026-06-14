package com.mobile.getit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.getit.R
import com.mobile.getit.domain.model.getCategoryResId
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.ui.viewmodel.ProductViewModel
import com.mobile.getit.utils.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(navController: NavController, categoryName: String, searchQuery: String, productVm: ProductViewModel) {
    val allProducts by productVm.products.collectAsState()

    val filteredProducts = remember(categoryName, searchQuery, allProducts) {
        allProducts.filter { product ->
            val matchCat = categoryName == "Semua" || categoryName == "empty" || product.category.equals(categoryName, ignoreCase = true)
            val matchSrc = searchQuery == "empty" || product.title.contains(searchQuery, ignoreCase = true)
            // Menampilkan semua produk (termasuk yang terjual) sesuai permintaan user
            matchCat && matchSrc
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val titleText = if (searchQuery != "empty") {
                        "Cari: '$searchQuery'"
                    } else if (categoryName != "empty") {
                        stringResource(getCategoryResId(categoryName))
                    } else {
                        "Produk"
                    }
                    Text(
                        text = titleText, 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = { 
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Produk tidak ditemukan.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screens.Detail.createRoute(product.id)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box {
                                AsyncImage(
                                    model = product.images.firstOrNull(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Label Status (Tersedia / Terjual)
                                Surface(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd),
                                    color = if (product.isSold) Color.Red.copy(alpha = 0.8f) else Color(0xFF2E7D32).copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (product.isSold) stringResource(R.string.status_sold) else stringResource(R.string.status_available),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(getCategoryResId(product.category)),
                                style = MaterialTheme.typography.labelSmall, 
                                color = Color.Gray
                            )
                            Text(
                                text = product.title, 
                                style = MaterialTheme.typography.titleSmall,
                                color = GetItPurple,
                                fontWeight = FontWeight.Bold, 
                                maxLines = 1, 
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatRupiah(product.price), 
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
