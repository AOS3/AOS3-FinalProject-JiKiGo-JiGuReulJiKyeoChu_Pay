package com.protect.jikigo_pay.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.protect.jikigo_pay.model.Pay
import com.protect.jikigo_pay.model.UserQR
import com.protect.jikigo_pay.pay.PayAppRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayViewModel @Inject constructor(
    private val payAppRepo: PayAppRepo
) : ViewModel() {
    private val _userInfo = MutableLiveData<UserQR?>()
    val userInfo: LiveData<UserQR?> = _userInfo

    private val _paymentPrice = MutableLiveData<Int>()
    val paymentPrice: LiveData<Int> = _paymentPrice


    fun getUserQR(userQR: String, pay: Pay) {
        viewModelScope.launch {
            val userQR = payAppRepo.getUserQrDocId(userQR, pay)
            Log.d("getUserQR", "${userQR}")
        }
    }

    fun paymentQR(pay: Pay) {
        viewModelScope.launch {
            _paymentPrice.postValue(pay.payPrice)
            payAppRepo.updateQRInfo(_userInfo.value!!, pay)
            Log.d("viewModel", "paymentQR안의 userInfo ${_userInfo.value!!}")
        }
    }
}