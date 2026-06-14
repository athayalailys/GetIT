package com.mobile.getit.utils

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
    return "Rp${formatter.format(amount)}"
}
