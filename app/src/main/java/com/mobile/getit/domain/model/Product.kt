package com.mobile.getit.domain.model

import com.google.firebase.database.PropertyName
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Product(
    val id: String = "",
    val title: String = "",
    val price: Int = 0,
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerWhatsapp: String = "",
    val sellerLocation: String = "",
    val sellerPhotoUri: String = "",
    val sellerBankName: String = "",
    val sellerBankAcc: String = "",
    val sellerEWalletName: String = "",
    val sellerEWalletAcc: String = "",
    val source: String = "FIREBASE",

    @get:PropertyName("bestOffer") @set:PropertyName("bestOffer")
    var isBestOffer: Boolean = false,

    @get:PropertyName("sold") @set:PropertyName("sold")
    var isSold: Boolean = false,

    @get:PropertyName("paid") @set:PropertyName("paid")
    var isPaid: Boolean = false,

    @get:PropertyName("handedOver") @set:PropertyName("handedOver")
    var isHandedOver: Boolean = false,

    @get:PropertyName("received") @set:PropertyName("received")
    var isReceived: Boolean = false,

    @get:PropertyName("inCart") @set:PropertyName("inCart")
    var isInCart: Boolean = false,

    val buyerId: String = "",
    val lastCartInteractionTimestamp: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "",
    val notes: String = ""
) {
    @Exclude
    fun getDisplayImage(): String {
        return when {
            imageUrl.isNotEmpty() -> imageUrl
            images.isNotEmpty() -> images.first()
            else -> ""
        }
    }

    @Exclude
    fun isFinished(): Boolean = isReceived
    
    @Exclude
    fun isTransactionFinished(): Boolean = isReceived
}
