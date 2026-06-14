package com.mobile.getit.ui.screens

import android.widget.Toast
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.getit.R
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.ui.theme.GetItYellow
import com.mobile.getit.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, productVm: ProductViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    val uploadStatus by productVm.uploadStatus.collectAsStateWithLifecycle()
    val categories by productVm.categories.collectAsStateWithLifecycle()
    
    // PERBAIKAN: Gunakan rememberSaveable agar input tidak hilang saat rotasi
    var namaProduk by rememberSaveable { mutableStateOf("") }
    var hargaProduk by rememberSaveable { mutableStateOf("") }
    var deskripsi by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    var selectedCategoryTechName by remember(categories) { 
        mutableStateOf(categories.firstOrNull()?.technicalName ?: "") 
    }
    
    val selectedCategoryDisplay = categories.find { it.technicalName == selectedCategoryTechName }?.let { 
        stringResource(it.nameResId) 
    } ?: ""

    // imageUrls manual management with rememberSaveable (using list)
    val imageUrls = remember { mutableStateListOf<String>() }
    var urlInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uploadStatus) {
        if (uploadStatus == true) {
            Toast.makeText(context, context.getString(R.string.toast_add_success), Toast.LENGTH_SHORT).show()
            productVm.resetUploadStatus()
            navController.popBackStack()
        } else if (uploadStatus == false) {
            Toast.makeText(context, "Gagal mengunggah produk", Toast.LENGTH_SHORT).show()
            productVm.resetUploadStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_product_title), color = Color.White) },
                navigationIcon = { 
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GetItPurple)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            Text(stringResource(R.string.product_url_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(stringResource(R.string.product_url_placeholder)) },
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
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                value = namaProduk, 
                onValueChange = { namaProduk = it }, 
                label = { Text(stringResource(R.string.product_name)) }, 
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = hargaProduk, 
                onValueChange = { hargaProduk = it }, 
                label = { Text(stringResource(R.string.product_price)) }, 
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.product_category_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
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

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (namaProduk.isNotEmpty() && hargaProduk.isNotEmpty() && imageUrls.isNotEmpty()) {
                        // PERBAIKAN Clean Architecture: Serahkan logika pembuatan objek Product ke ViewModel
                        productVm.addProduct(
                            nama = namaProduk,
                            harga = hargaProduk,
                            deskripsi = deskripsi,
                            category = selectedCategoryTechName,
                            imageUrls = imageUrls.toList()
                        )
                    } else {
                        Toast.makeText(context, context.getString(R.string.form_incomplete_msg), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GetItYellow),
                enabled = uploadStatus == null
            ) { 
                if (uploadStatus != null) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text(stringResource(R.string.btn_add), color = Color.Black, fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}
