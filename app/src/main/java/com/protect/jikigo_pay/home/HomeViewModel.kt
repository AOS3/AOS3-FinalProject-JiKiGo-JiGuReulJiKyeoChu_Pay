package com.protect.jikigo_pay.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.protect.Pay

import com.protect.jikigo_pay.pay.PayAppRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val payAppRepo: PayAppRepo
) : ViewModel() {

    init {
        getPayStore()
    }

    private val _payStore = MutableLiveData<List<Pay>>()
    val payStore: LiveData<List<Pay>> = _payStore

    fun getPayStore() {
        viewModelScope.launch {
            val payStore = payAppRepo.getPayStore()
            _payStore.postValue(payStore)
        }
    }

}