package com.mobile.getit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.components.CustomTextField
import com.mobile.getit.ui.theme.GetItPurple
import com.mobile.getit.ui.theme.GetItYellow
import com.mobile.getit.ui.viewmodel.AuthState
import com.mobile.getit.ui.viewmodel.AuthViewModel
import com.mobile.getit.ui.viewmodel.ViewModelFactory
import com.mobile.getit.domain.model.User

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory(context) }
    val authVm: AuthViewModel = viewModel(factory = factory)
    val authState by authVm.authState.collectAsStateWithLifecycle()

    var namaLengkap by rememberSaveable { mutableStateOf("") }
    var alamatEmail by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authVm.checkInitialAuthStatus()
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Verified -> {
                // PERBAIKAN: Setelah verifikasi, arahkan ke Login (Poin 4)
                Toast.makeText(context, "Email berhasil diverifikasi! Silakan masuk.", Toast.LENGTH_LONG).show()
                navController.navigate(Screens.Login.route) {
                    popUpTo(Screens.Register.route) { inclusive = true }
                }
                authVm.resetAuthState()
            }
            is AuthState.Success -> {
                // Sesuai permintaan: Halaman register tidak ada toast saat sukses (sudah pindah ke Verified/Login)
                navController.navigate(Screens.Home.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                authVm.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                authVm.resetAuthState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daftar", fontSize = 32.sp, color = GetItPurple, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            CustomTextField("Nama Lengkap", namaLengkap) { namaLengkap = it }
            CustomTextField("Alamat Email", alamatEmail) { alamatEmail = it }
            CustomTextField("Username", username) { username = it }
            CustomTextField("Password", password, isPassword = true) { password = it }
            CustomTextField("Konfirmasi Password", confirmPassword, isPassword = true) { confirmPassword = it }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (password == confirmPassword) {
                        authVm.register(User(fullName = namaLengkap, email = alamatEmail, username = username, password = password))
                    } else {
                        Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GetItYellow),
                shape = RoundedCornerShape(12.dp),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("Daftar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (authState is AuthState.WaitingForEmail) {
            AlertDialog(
                onDismissRequest = { },
                properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                title = { Text("Verifikasi Email", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GetItPurple)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Link verifikasi terkirim. Klik link di email Anda untuk melanjutkan.", textAlign = TextAlign.Center)
                        TextButton(onClick = { authVm.resendEmail() }) {
                            Text("Kirim Ulang Email", color = GetItPurple)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { authVm.cancelRegistration() }) {
                        Text("Batal", color = Color.Red)
                    }
                }
            )
        }
    }
}
