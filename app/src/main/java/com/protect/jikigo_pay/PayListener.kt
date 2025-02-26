package com.protect.jikigo_pay

import com.protect.Pay

interface PayListener {
    fun onClickPay(pay: Pay)
}