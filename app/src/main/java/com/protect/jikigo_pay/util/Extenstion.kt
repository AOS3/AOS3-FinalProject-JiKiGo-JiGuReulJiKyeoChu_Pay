package com.protect.jikigo_pay.util

import android.icu.text.DecimalFormat
import android.widget.TextView

fun TextView.applyNumberFormat(amount: Int) {
    text = amount.convertThreeDigitComma()
}

private fun Int.convertThreeDigitComma(): String {
    val decimalFormat = DecimalFormat("#,###원")
    return decimalFormat.format(this)


}
