package com.protect.jikigo_pay.pay

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.protect.jikigo_pay.model.Pay
import com.protect.jikigo_pay.R
import com.protect.jikigo_pay.model.UserQR
import com.protect.jikigo_pay.databinding.ActivityPayBinding
import com.protect.jikigo_pay.viewmodel.PayViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class PayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPayBinding
    private val CAMERA_REQUEST_CODE = 100
    private var pay: Pay? = null
    private val viewModel: PayViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            setQRScanner()
        }
        pay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("pay_data", Pay::class.java) // 최신 방식 (API 33 이상)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("pay_data") // 기존 방식 (API 32 이하)
        }
        // pay 사용 예제
        pay?.let {
            println("Pay 정보: ${it}")
        } ?: run {
            println("Pay 데이터가 없습니다.")
        }
        restartScanner()
        setLayout()
    }


    override fun onResume() {
        super.onResume()
        binding.qrScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.qrScanner.pause()
    }

    private fun setLayout() {
        onClickToolbar()
    }

    private fun onClickToolbar() {
        binding.toolbarPay.setNavigationOnClickListener {
            finish()
        }
    }

    private fun restartScanner() {
        binding.btnQr.setOnClickListener {
            binding.qrScanner.resume()
        }
    }

    private fun setQRScanner() {
        binding.qrScanner.decodeContinuous { result ->
            result?.let {
                binding.qrScanner.pause() // 스캔 후 일시 정지 -> 결제
                Log.d("barcode", it.text)
                getJson(it.text)
                userQrCode()
            }
        }
    }

    private fun getJson(json: String) {
        val jsonObject = JSONObject(json)
        val paymentDate: String = jsonObject.getString("paymentDate")
        val payName: String = jsonObject.getString("payName")
        val paymentPrice: Int = jsonObject.getInt("paymentPrice")
        val userId: String = jsonObject.getString("userId")
        val userQR: String = jsonObject.getString("userQR")
        val userQrError: String = jsonObject.getString("userQrError")
        val userQrUse: Boolean = jsonObject.getBoolean("userQrUse")

        Log.d("getJson", "payName: $payName")
        Log.d("getJson", "date: $paymentDate")
        Log.d("getJson", "price: $paymentPrice")
        Log.d("getJson", "userId: $userId")
        Log.d("getJson", "userQR: $userQR")
        Log.d("getJson", "userQRError: $userQrError")
        Log.d("getJson", "userQrUse: $userQrUse")
        viewModel.getUserQR(userQR, pay!!)
    }


    //
    private fun userQrCode() {
        viewModel.userInfo.observe(this) {
            Log.d("userQRCode", "${it}")
            processScannedQRCode(it!!)
        }
    }

    private fun processScannedQRCode(userQR: UserQR) {
        try {
            // 서버로 결제 요청 전송
            sendPaymentRequest(userQR)
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "잘못된 QR 코드입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendPaymentRequest(userQR: UserQR) {
        Log.d("request", userQR.toString())
        Toast.makeText(this, "${userQR}", Toast.LENGTH_SHORT).show()
        if (!userQR.userQrUse) {
            //결제
            Log.d("가져온pay", "$pay")
            viewModel.paymentQR(pay!!)
        } else {
            // 금액 - 막기 // 이미 결제 된 QR
            //  viewModel.paymentQRError()
        }

    }
}