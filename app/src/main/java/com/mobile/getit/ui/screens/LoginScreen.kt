package com.mobile.getit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory(context) }
    val authVm: AuthViewModel = viewModel(factory = factory)
    val authState by authVm.authState.collectAsStateWithLifecycle()
    
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authVm.checkInitialAuthStatus()
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Berhasil Masuk!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screens.Home.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                authVm.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                authVm.resetAuthState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(GetItPurple)) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Masuk", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("ke akun Anda", color = Color.White.copy(0.8f), fontSize = 16.sp)
            }
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
            ) {
                Column(modifier = Modifier.padding(30.dp)) {
                    CustomTextField(
                        label = "Username atau Email",
                        value = identifier,
                        onValueChange = { identifier = it }
                    )

                    CustomTextField(
                        label = "Kata Sandi",
                        value = password,
                        isPassword = true,
                        onValueChange = { password = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            if (identifier.isNotEmpty() && password.isNotEmpty()) {
                                authVm.login(identifier, password)
                            } else {
                                Toast.makeText(context, "Username/Email dan Password wajib diisi!", Toast.LENGTH_SHORT).show()
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
                            Text("Masuk", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { navController.navigate(Screens.Register.route) }, 
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        enabled = authState !is AuthState.Loading
                    ) {
                        Text("Belum punya akun? Daftar sekarang", color = GetItPurple)
                    }
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
                        Text("Akun Anda belum terverifikasi. Silakan klik link yang dikirim ke email Anda.", textAlign = TextAlign.Center)
                        TextButton(onClick = { authVm.resendEmail() }) {
                            Text("Kirim Ulang Email", color = GetItPurple)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { authVm.logout { } }) { 
                        Text("Batal / Ganti Akun", color = Color.Red) 
                    }
                }
            )
        }
    }
}
