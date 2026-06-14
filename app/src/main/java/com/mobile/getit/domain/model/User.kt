package com.mobile.getit.domain.model

import com.google.firebase.database.PropertyName

data class User(
    val fullName: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val location: String = "",
    val photoUri: String = "",
    
    @get:PropertyName("verified") @set:PropertyName("verified")
    var isVerified: Boolean = false,
    
    val type: String = "EMAIL", // EMAIL or GOOGLE

    // Informasi Penjual / Pembayaran (Penting untuk C2C)
    val whatsapp: String = "",
    val bankName: String = "",
    val bankAcc: String = "",
    
    @get:PropertyName("ewalletName") @set:PropertyName("ewalletName")
    var eWalletName: String = "",
    
    @get:PropertyName("ewalletAcc") @set:PropertyName("ewalletAcc")
    var eWalletAcc: String = ""
)
