package com.mobile.getit.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.getit.R
import com.mobile.getit.domain.model.getCategoryResId
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.ui.theme.GetItYellow
import com.mobile.getit.ui.viewmodel.ProductViewModel
import com.mobile.getit.ui.viewmodel.WishlistViewModel
import com.mobile.getit.utils.formatRupiah
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, productId: String, productVm: ProductViewModel, wishlistVm: WishlistViewModel) {
    val context = LocalContext.current
    val productList by productVm.products.collectAsStateWithLifecycle()
    val currentUser by productVm.currentUser.collectAsStateWithLifecycle()
    val wishlistItems by wishlistVm.wishlistItems.collectAsStateWithLifecycle()

    val product = productList.find { it.id == productId }
    val isInWishlist = wishlistItems.any { it.id == productId }

    val codText = stringResource(R.string.payment_cod)
    val bankText = stringResource(R.string.payment_bank)
    val ewalletText = stringResource(R.string.payment_ewallet)

    var selectedPaymentMethod by remember { mutableStateOf(codText) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.detail_title), color = GetItPurple, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screens.Wishlist.route) }) {
                        Icon(Icons.Default.ShoppingCart, "Cart", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (product != null && !product.isSold && !product.isReceived) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.detail_total_price), fontSize = 12.sp, color = Color.Gray)
                            Text(formatRupiah(product.price), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = {
                                if (selectedPaymentMethod == codText) {
                                    productVm.updateTransaction(
                                        id = product.id, 
                                        isSold = true, 
                                        buyerId = currentUser?.username ?: "",
                                        paymentMethod = codText
                                    )
                                    Toast.makeText(context, context.getString(R.string.toast_cod_success), Toast.LENGTH_LONG).show()
                                } else {
                                    showPaymentDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GetItYellow),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(54.dp).width(150.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.detail_btn_buy), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (product == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().height(320.dp).padding(16.dp)) {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        items(product.images) { img ->
                            AsyncImage(
                                model = img,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(stringResource(getCategoryResId(product.category)), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(product.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GetItPurple, modifier = Modifier.weight(1f))
                        
                        IconButton(
                            onClick = { 
                                val targetInCart = !isInWishlist
                                // Gunakan updateCartState yang eksplisit untuk mencegah status terbalik
                                productVm.updateCartState(product.id, targetInCart)
                                
                                if (targetInCart) {
                                    wishlistVm.addToWishlist(product)
                                } else {
                                    wishlistVm.deleteFromWishlist(product.id)
                                }
                            },
                            modifier = Modifier.size(44.dp).background(if(isInWishlist) GetItYellow else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart, 
                                contentDescription = "Add to Cart", 
                                tint = if(isInWishlist) Color.Black else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    if (product.sellerId.isNotEmpty()) {
                                        navController.navigate(Screens.Profile.createRoute(product.sellerId))
                                    }
                                }
                        ) {
                            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                if (product.sellerPhotoUri.isNotEmpty()) {
                                    AsyncImage(model = product.sellerPhotoUri, contentDescription = null, contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = Color.Gray)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(product.sellerName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(if (product.sellerLocation.isNotEmpty()) product.sellerLocation else "Banjarmasin", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        
                        IconButton(onClick = {
                            var phone = product.sellerWhatsapp.replace(Regex("[^0-9]"), "")
                            
                            if (phone.startsWith("0")) {
                                phone = "62" + phone.substring(1)
                            }

                            if (phone.isEmpty()) {
                                Toast.makeText(context, "Nomor WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            
                            val waUrl = "https://wa.me/$phone"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, context.getString(R.string.toast_wa_error), Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, stringResource(R.string.chat_seller), tint = GetItPurple)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(stringResource(R.string.detail_payment_method), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentSelectableTag(codText, selectedPaymentMethod) { selectedPaymentMethod = it }
                        if(product.sellerBankAcc.isNotEmpty()) {
                            PaymentSelectableTag(bankText, selectedPaymentMethod) { selectedPaymentMethod = it }
                        }
                        if(product.sellerEWalletAcc.isNotEmpty()) {
                            PaymentSelectableTag(ewalletText, selectedPaymentMethod) { selectedPaymentMethod = it }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.detail_description_label), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = product.description, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        modifier = Modifier.padding(top = 8.dp), 
                        lineHeight = 20.sp,
                        fontSize = 13.sp
                    )
                    
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }

    if (showPaymentDialog && product != null) {
        Dialog(onDismissRequest = { showPaymentDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    IconButton(
                        onClick = { showPaymentDialog = false },
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp).size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Payments, null, tint = GetItPurple, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val msg = if (selectedPaymentMethod == bankText) {
                            stringResource(R.string.payment_dialog_bank_msg, product.sellerBankName, product.sellerBankAcc)
                        } else {
                            stringResource(R.string.payment_dialog_ewallet_msg, product.sellerEWalletName, product.sellerEWalletAcc)
                        }
                        
                        Text(
                            text = msg, 
                            textAlign = TextAlign.Center, 
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                productVm.updateTransaction(
                                    id = product.id, 
                                    isPaid = true, 
                                    isSold = true, 
                                    buyerId = currentUser?.username ?: "",
                                    paymentMethod = selectedPaymentMethod
                                )
                                showPaymentDialog = false
                                Toast.makeText(context, context.getString(R.string.toast_payment_sent), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GetItPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.btn_done), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSelectableTag(text: String, current: String, onSelect: (String) -> Unit) {
    val isSelected = text == current
    Surface(
        onClick = { onSelect(text) },
        color = if (isSelected) GetItYellow else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text, 
            fontSize = 11.sp, 
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
