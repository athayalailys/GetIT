package com.mobile.getit.domain.model

import com.mobile.getit.R

data class Category(
    val id: String,
    val technicalName: String,
    val nameResId: Int,
    val iconEmoji: String
)

fun getCategoryResId(technicalName: String): Int {
    return when (technicalName) {
        "Laptop dan Komputer" -> R.string.cat_laptop
        "Komponen untuk PC" -> R.string.cat_pc
        "Aksesoris Komputer" -> R.string.cat_acc
        "Gadget dan Mobile" -> R.string.cat_gadget
        "Semua" -> R.string.cat_all
        else -> R.string.app_name
    }
}
