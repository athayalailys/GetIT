package com.mobile.getit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.getit.R
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.ui.theme.GetItYellow
import com.mobile.getit.ui.viewmodel.ProductViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(navController: NavController, productId: String, productVm: ProductViewModel) {
    val context = LocalContext.current
    val productList by productVm.products.collectAsStateWithLifecycle()
    val categories by productVm.categories.collectAsStateWithLifecycle()
    val currentUser by productVm.currentUser.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    val originalProduct = remember(productList, productId) {
        productList.find { it.id == productId }
    }

    var nama by rememberSaveable { mutableStateOf("") }
    var harga by rememberSaveable { mutableStateOf("") }
    var deskripsi by rememberSaveable { mutableStateOf("") }
    var selectedCategoryTechName by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val imageUrls = remember { mutableStateListOf<String>() }
    var urlInput by rememberSaveable { mutableStateOf("") }

    val selectedCategoryDisplay = categories.find { it.technicalName == selectedCategoryTechName }?.let { 
        stringResource(it.nameResId) 
    } ?: ""

    LaunchedEffect(originalProduct) {
        originalProduct?.let {
            if (nama.isEmpty()) nama = it.title
            if (harga.isEmpty()) harga = it.price.toString()
            if (deskripsi.isEmpty()) deskripsi = it.description
            if (selectedCategoryTechName.isEmpty()) selectedCategoryTechName = it.category
            if (imageUrls.isEmpty()) {
                imageUrls.clear()
                imageUrls.addAll(it.images)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_product_title), color = Color.White) },
                navigationIcon = { 
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GetItPurple)
            )
        }
    ) { paddingValues ->
        if (originalProduct == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(20.dp).verticalScroll(scrollState)
            ) {
                Text(stringResource(R.string.upload_image_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Paste URL Gambar") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = imageUrls.size < 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (urlInput.isNotEmpty()) {
                                imageUrls.add(urlInput)
                                urlInput = ""
                            }
                        },
                        modifier = Modifier.background(GetItPurple, CircleShape),
                        enabled = urlInput.isNotEmpty() && imageUrls.size < 3
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah URL", tint = Color.White)
                    }
                }

                LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(imageUrls) { index, url ->
                        Box(modifier = Modifier.size(100.dp)) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUrls.removeAt(index) },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Red, CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nama, 
                    onValueChange = { nama = it }, 
                    label = { Text(stringResource(R.string.product_name)) }, 
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = harga, 
                    onValueChange = { harga = it }, 
                    label = { Text(stringResource(R.string.product_price)) }, 
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.product_category_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategoryDisplay,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(stringResource(cat.nameResId)) }, 
                                onClick = {
                                    selectedCategoryTechName = cat.technicalName
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = deskripsi, 
                    onValueChange = { deskripsi = it }, 
                    label = { Text(stringResource(R.string.product_description)) }, 
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { 
                            productVm.deleteProduct(productId)
                            Toast.makeText(context, context.getString(R.string.toast_product_deleted), Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }, 
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(stringResource(R.string.btn_delete)) }
                    
                    Button(
                        onClick = { 
                            if (nama.isNotEmpty() && harga.isNotEmpty() && imageUrls.isNotEmpty()) {
                                val user = currentUser
                                val updatedProduct = originalProduct.copy(
                                    title = nama,
                                    price = harga.toIntOrNull() ?: originalProduct.price,
                                    description = deskripsi,
                                    category = selectedCategoryTechName,
                                    images = imageUrls.toList(),
                                    imageUrl = imageUrls.firstOrNull() ?: "", // FIX: Update main imageUrl
                                    sellerName = user?.fullName ?: originalProduct.sellerName,
                                    sellerLocation = user?.location ?: originalProduct.sellerLocation,
                                    sellerPhotoUri = user?.photoUri ?: originalProduct.sellerPhotoUri,
                                    sellerWhatsapp = user?.whatsapp ?: originalProduct.sellerWhatsapp,
                                    sellerBankName = user?.bankName ?: originalProduct.sellerBankName,
                                    sellerBankAcc = user?.bankAcc ?: originalProduct.sellerBankAcc,
                                    sellerEWalletName = user?.eWalletName ?: originalProduct.sellerEWalletName,
                                    sellerEWalletAcc = user?.eWalletAcc ?: originalProduct.sellerEWalletAcc
                                )
                                productVm.updateProduct(productId, updatedProduct)
                                Toast.makeText(context, "Berhasil memperbarui produk", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, context.getString(R.string.form_incomplete_msg), Toast.LENGTH_SHORT).show()
                            }
                        }, 
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GetItYellow),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(stringResource(R.string.btn_update), color = Color.Black, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
