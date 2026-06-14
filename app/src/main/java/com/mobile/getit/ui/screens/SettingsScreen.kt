package com.mobile.getit.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.getit.R
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.ui.viewmodel.SettingsViewModel
import com.mobile.getit.ui.viewmodel.ViewModelFactory
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory(context) }
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var showLanguageMenu by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            val internalUri = saveImageToInternalStorage(context, it)
            viewModel.updatePhoto(internalUri.toString())
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = Color.White) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Photo Section
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(100.dp)) {
                    if (uiState.photoUri.isNotEmpty()) {
                        AsyncImage(
                            model = uiState.photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = Color.White)
                        }
                    }
                    IconButton(
                        onClick = { photoLauncher.launch("image/*") },
                        modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).background(GetItPurple, CircleShape)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Section
            SectionHeader(icon = Icons.Default.AccountCircle, title = stringResource(R.string.settings_profile_section))
            OutlinedTextField(
                value = uiState.fullName, 
                onValueChange = { viewModel.onFullNameChange(it) }, 
                label = { Text(stringResource(R.string.settings_label_fullname)) }, 
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.location, 
                onValueChange = { viewModel.onLocationChange(it) }, 
                label = { Text(stringResource(R.string.settings_label_location)) }, 
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GetItPurple)
            ) { Text(stringResource(R.string.settings_btn_save_profile)) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Payment Section
            SectionHeader(icon = Icons.Default.Payments, title = stringResource(R.string.settings_payment_section))
            OutlinedTextField(
                value = uiState.whatsapp, 
                onValueChange = { viewModel.onWhatsappChange(it) }, 
                label = { Text(stringResource(R.string.settings_label_wa)) }, 
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.bankName, 
                    onValueChange = { viewModel.onBankNameChange(it) }, 
                    label = { Text(stringResource(R.string.settings_label_bank)) }, 
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.bankAcc, 
                    onValueChange = { viewModel.onBankAccChange(it) }, 
                    label = { Text(stringResource(R.string.settings_label_acc)) }, 
                    modifier = Modifier.weight(1.5f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.eWalletName, 
                    onValueChange = { viewModel.onEWalletNameChange(it) }, 
                    label = { Text(stringResource(R.string.settings_label_ewallet)) }, 
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.eWalletAcc, 
                    onValueChange = { viewModel.onEWalletAccChange(it) }, 
                    label = { Text(stringResource(R.string.settings_label_ewallet_acc)) }, 
                    modifier = Modifier.weight(1.5f)
                )
            }
            Button(
                onClick = { viewModel.savePaymentMethods() }, 
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GetItPurple)
            ) { Text(stringResource(R.string.settings_btn_save_payment)) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Verification Section
            SectionHeader(icon = Icons.Default.Security, title = stringResource(R.string.settings_verification_section))
            if (uiState.isVerified) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Text(stringResource(R.string.settings_verified_success), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                }
            } else {
                var emailUlm by remember { mutableStateOf("") }
                OutlinedTextField(value = emailUlm, onValueChange = { emailUlm = it }, label = { Text(stringResource(R.string.settings_label_email_ulm)) }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { 
                    if(emailUlm.endsWith("@mhs.ulm.ac.id") || emailUlm.endsWith("@ulm.ac.id")) showOtpDialog = true 
                    else Toast.makeText(context, "Gunakan Email Resmi ULM!", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(stringResource(R.string.settings_btn_verify)) }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Appearance Section
            SectionHeader(icon = Icons.Default.Tune, title = stringResource(R.string.settings_appearance_section))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.settings_dark_mode_label), modifier = Modifier.weight(1f))
                Switch(checked = uiState.isDarkTheme, onCheckedChange = {
                    viewModel.setDarkMode(it)
                })
            }
            
            Box {
                OutlinedTextField(
                    value = if(uiState.language == "in") "Indonesia" else "English",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.settings_language_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { showLanguageMenu = true }) }
                )
                DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                    DropdownMenuItem(text = { Text("Indonesia") }, onClick = {
                        viewModel.setLanguage("in")
                        showLanguageMenu = false
                    })
                    DropdownMenuItem(text = { Text("English") }, onClick = {
                        viewModel.setLanguage("en")
                        showLanguageMenu = false
                    })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = {
                    viewModel.logout {
                        navController.navigate(Screens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GetItPurple)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_btn_logout))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delete Account Button
            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red)
            ) {
                Icon(Icons.Default.DeleteForever, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_btn_delete_acc))
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Dialogs
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Verifikasi Email") },
            text = { OutlinedTextField(value = otpInput, onValueChange = { if(it.length <= 6) otpInput = it }, label = { Text("Kode 6 Digit") }) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.verifyUlm(otpInput) { success ->
                        if (success) {
                            showOtpDialog = false
                            Toast.makeText(context, "Identitas Terverifikasi!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "OTP Salah!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("Verifikasi") }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(stringResource(R.string.settings_delete_title)) },
            text = { Text(stringResource(R.string.settings_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount { success, error ->
                        if (success) {
                            navController.navigate(Screens.Login.route) { popUpTo(0) }
                            Toast.makeText(context, "Akun dihapus permanen.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, error ?: "Gagal hapus akun", Toast.LENGTH_LONG).show()
                        }
                    }
                    showDeleteAccountDialog = false
                }) { Text(stringResource(R.string.settings_btn_confirm_delete), color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text(stringResource(R.string.settings_btn_cancel_delete)) }
            }
        )
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): Uri {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.filesDir, "profile_pic.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return Uri.fromFile(file)
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
        Icon(icon, contentDescription = null, tint = GetItPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GetItPurple)
    }
}
