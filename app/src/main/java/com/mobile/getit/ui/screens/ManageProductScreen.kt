package com.mobile.getit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.getit.R
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.viewmodel.ProductViewModel
import com.mobile.getit.utils.formatRupiah

@Composable
fun ManageProductScreen(navController: NavController, productVm: ProductViewModel) {
    val context = LocalContext.current
    val productList by productVm.products.collectAsStateWithLifecycle()
    val currentUser by productVm.currentUser.collectAsStateWithLifecycle()

    val myProducts = remember(productList, currentUser) {
        val username = currentUser?.username ?: ""
        productList.filter { it.sellerId == username }
    }

    val isVerified = currentUser?.isVerified == true || 
                     currentUser?.email?.endsWith("@mhs.ulm.ac.id") == true || 
                     currentUser?.email?.endsWith("@ulm.ac.id") == true

    Scaffold(
        bottomBar = { GetItBottomBar(navController, Screens.ManageProduct.route) }
    ) { paddingValues ->
        if (!isVerified) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_account_warning), textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                item {
                    Text(stringResource(R.string.manage_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.manage_subtitle), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                if (myProducts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.manage_empty), color = Color.Gray)
                        }
                    }
                }

                items(myProducts, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = product.getDisplayImage(),
                                contentDescription = null,
                                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = product.title, 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 15.sp, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        IconButton(
                                            onClick = { navController.navigate(Screens.EditProduct.createRoute(product.id)) },
                                            modifier = Modifier.size(24.dp)
                                        ) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) }

                                        IconButton(
                                            onClick = { 
                                                productVm.deleteProduct(product.id)
                                                Toast.makeText(context, context.getString(R.string.toast_product_deleted), Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
                                    }
                                }

                                Text(formatRupiah(product.price), fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                
                                val statusText = when {
                                    product.isReceived -> stringResource(R.string.status_finished)
                                    product.isHandedOver -> stringResource(R.string.status_handed_over)
                                    product.isSold -> stringResource(R.string.status_sold)
                                    else -> stringResource(R.string.status_available)
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Surface(
                                        color = if(product.isReceived) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(statusText, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = if(product.isReceived) Color(0xFF2E7D32) else Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                    }
                                    
                                    if (!product.isReceived) {
                                        Spacer(Modifier.width(8.dp))
                                        TextButton(
                                            onClick = { 
                                                val (nextSold, nextHandover) = when {
                                                    !product.isSold -> true to false
                                                    !product.isHandedOver -> true to true
                                                    else -> true to true
                                                }
                                                productVm.updateTransaction(
                                                    id = product.id, 
                                                    isSold = nextSold, 
                                                    isHandedOver = nextHandover
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) { 
                                            val btnText = when {
                                                !product.isSold -> stringResource(R.string.btn_mark_sold)
                                                !product.isHandedOver -> stringResource(R.string.btn_mark_handed_over)
                                                else -> ""
                                            }
                                            if (btnText.isNotEmpty()) {
                                                Text(btnText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
}
