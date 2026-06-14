package com.mobile.getit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.mobile.getit.ui.theme.GetItYellow
import com.mobile.getit.ui.viewmodel.ProductViewModel
import com.mobile.getit.ui.viewmodel.WishlistViewModel
import com.mobile.getit.utils.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(navController: NavController, wishlistVm: WishlistViewModel, productVm: ProductViewModel) {
    val context = LocalContext.current
    val items by wishlistVm.wishlistItems.collectAsStateWithLifecycle()
    val allProducts by productVm.products.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { 
        productVm.loadMarketplaceProducts()
    }

    val validItems = remember(items, allProducts) {
        items.filter { localItem -> 
            allProducts.any { it.id == localItem.id }
        }
    }

    LaunchedEffect(items, allProducts) {
        if (allProducts.isNotEmpty()) {
            items.forEach { localItem ->
                if (allProducts.none { it.id == localItem.id }) {
                    wishlistVm.deleteFromWishlist(localItem.id)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wishlist_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = { GetItBottomBar(navController, Screens.Wishlist.route) }
    ) { paddingValues ->
        if (validItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.wishlist_empty), color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(validItems) { wishlistProduct ->
                    val product = allProducts.find { it.id == wishlistProduct.id } ?: wishlistProduct
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Screens.Detail.createRoute(product.id)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = product.images.firstOrNull(),
                                    contentDescription = null,
                                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(product.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                                    Text(formatRupiah(product.price), style = MaterialTheme.typography.bodyMedium, color = GetItPurple, fontWeight = FontWeight.ExtraBold)
                                }
                                if (!product.isPaid) {
                                    IconButton(onClick = { wishlistVm.deleteFromWishlist(product.id) }) {
                                        Icon(Icons.Outlined.Delete, "Hapus", tint = Color.Red)
                                    }
                                }
                            }
                            
                            if (product.isPaid) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusMsg = when {
                                        product.isReceived -> stringResource(R.string.wishlist_status_done)
                                        product.isHandedOver -> stringResource(R.string.wishlist_status_handed_over)
                                        else -> stringResource(R.string.wishlist_status_waiting)
                                    }
                                    Text(statusMsg, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    
                                    if (product.isHandedOver && !product.isReceived) {
                                        Button(
                                            onClick = { 
                                                productVm.markAsReceived(product.id)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GetItYellow),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Text(stringResource(R.string.wishlist_btn_receive), fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
