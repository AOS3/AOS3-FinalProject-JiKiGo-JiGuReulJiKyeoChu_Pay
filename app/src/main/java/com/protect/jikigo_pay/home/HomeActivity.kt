package com.protect.jikigo_pay.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.protect.jikigo_pay.R
import com.protect.jikigo_pay.databinding.ActivityHomeBinding
import com.protect.jikigo_pay.model.Pay
import com.protect.jikigo_pay.pay.PayActivity
import com.protect.jikigo_pay.util.PayListener
import com.protect.jikigo_pay.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), PayListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: HomeAdapter
    private val viewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setRecycler()
    }


    private fun setRecycler() {
        adapter = HomeAdapter(this)
        binding.rvHome.adapter = adapter
        viewModel.payStore.observe(this) { payList ->
            Log.d("TEST", "${payList}")
            adapter.updateItems(payList)

        }
    }

    override fun onClickPay(pay: Pay) {
        Log.d("onClickPay", "${pay}")
        val intent = Intent(this, PayActivity::class.java)
        intent.putExtra("pay_data", pay)
        startActivity(intent)
    }


}