package com.protect.jikigo_pay.util

import com.protect.jikigo_pay.model.Pay

interface PayListener {
    fun onClickPay(pay: Pay)
}