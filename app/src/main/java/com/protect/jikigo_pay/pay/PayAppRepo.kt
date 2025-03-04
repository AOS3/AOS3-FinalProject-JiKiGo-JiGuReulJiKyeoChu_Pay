package com.protect.jikigo_pay.pay

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.protect.jikigo_pay.model.Pay
import com.protect.jikigo_pay.model.UserQR
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PayAppRepo @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realTime: FirebaseDatabase
) {
    // 모든 상점 정보를 가져옴
    suspend fun getPayStore(): List<Pay> {
        return try {
            val document = firestore.collection("PayApp")
                .get()
                .await()
            document.toObjects(Pay::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 전체 문서를 가져오는 메서드
    suspend fun getUserQrDocId(userQr: String, pay: Pay) {
        val realDB = realTime.getReference("UserInfo").child("userQR")
        try {
            val snapshot = realDB.get().await()
            if (snapshot.exists()) {
                for (childSnapshot in snapshot.children) {
                    val docId = childSnapshot.key
                    if (userQr == docId) {
                        getUserQrDocIdDetail(userQr, pay)
                        return
                    }
                    Log.d("getUserQrDocId", "docId: $docId")
                }
            } else {
                Log.d("snapshot", "No documents found")
            }
        } catch (e: Exception) {
            Log.d("snapshot", "NError getting documents")
        }
    }

    // 결제대기중인QR 문서를 가져오는 메서드
    suspend fun getUserQrDocIdDetail(userQr: String, pay: Pay) {
        val realDB = realTime.getReference("UserInfo").child("userQR").child(userQr)
        try {
            val snapshot = realDB.get().await()
            if (snapshot.exists()) {
                val qrData = snapshot.getValue(UserQR::class.java)
                Log.d("getUserQrDocIdDetail", "user.payName" + qrData?.payName)
                if (qrData != null) {
                    Log.d("getUser", "${qrData}")
                    updateUserQR(qrData, pay)
                }
            }
        } catch (e: Exception) {
            Log.d("getUserQrDocIdDetail", "$e")
        }
    }

    suspend fun updateUserQR(user: UserQR, pay: Pay) {
        val realDB = realTime.getReference("UserInfo").child("userQR").child(user.userQR)
        val updatePoint = if (pay.payName == "텀블러 인증(적립)") {
            user.userPoint + pay.payPrice
        } else {
            user.userPoint - pay.payPrice
        }

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

        // 기본 초기값 설정하여 오류 방지
        var updateData: Map<String, Any> = emptyMap()

        // 이미 사용된 QR 코드인지 확인
        if (user.userQrUse) {
            updateData = mapOf(
                "userQrUse" to true,
                "paymentPrice" to pay.payPrice,
                "userQrError" to "이미 사용 된 QR코드 입니다.",
                "userPoint" to user.userPoint,
            )
        }
        // 텀블러 인증 QR 코드 처리
        else if (pay.payName == "텀블러 인증(적립)") {
            if (user.payName == "텀블러 인증(적립)") {
                // 정상적인 텀블러 인증
                updateData = mapOf(
                    "userQrUse" to true,
                    "paymentPrice" to pay.payPrice,
                    "payName" to pay.payName,
                    "payType" to pay.payType,
                    "paymentDate" to sdf.format(Date()),
                    "userQrError" to "텀블러 인증 완료",
                    "userPoint" to updatePoint,
                )
            } else {
                // 현장 결제 리더기에 텀블러 인증 QR을 찍었을 때
                updateData = mapOf(
                    "userQrUse" to false,
                    "paymentPrice" to pay.payPrice,
                    "userQrError" to "해당 QR은 현장결제 전용입니다.",
                    "userPoint" to user.userPoint,
                )
            }
        }
        // 포인트 부족 시 오류 처리
        else if (updatePoint < 0) {
            Log.d("PayAppRepo","updatePoint: $updatePoint")
            updateData = mapOf(
                "userQrUse" to false,
                "paymentPrice" to pay.payPrice,
                "userQrError" to "포인트가 부족합니다.",
                "userPoint" to user.userPoint,
            )
        }
        // 🔵 현장 결제 QR 코드 처리
        else if (user.payName == "") {
            // 정상적인 현장 결제
            updateData = mapOf(
                "userQrUse" to true,
                "paymentPrice" to pay.payPrice,
                "payName" to pay.payName,
                "payType" to pay.payType,
                "paymentDate" to sdf.format(Date()),
                "userQrError" to "정상적으로 결제되었습니다.",
                "userPoint" to updatePoint,
            )
        }
        // 최종적으로 updateData가 초기화되지 않았다면 기본 오류 메시지 처리
        if (updateData.isEmpty()) {
            updateData = mapOf(
                "userQrUse" to false,
                "paymentPrice" to 0,
                "userQrError" to "해당 QR은 텀블러 인증 전용 입니다.",
                "userPoint" to user.userPoint,
            )
            Log.e("updateUserQR", " updateData가 초기화되지 않음! 기본 오류 메시지 처리됨.")
        }

        // 🔄 Firebase Realtime Database 업데이트
        realDB.updateChildren(updateData)
            .addOnSuccessListener {
                Log.d("updateDat", sdf.format(Date()))
                Log.d("updateUserQR", "정상적으로 업데이트 성공")
            }
            .addOnFailureListener { e ->
                Log.d("updateUserQR", "$e")
            }
    }

    // price는 가격 상태에 따라 추후 변경
    suspend fun updateQRInfo(userQR: UserQR, pay: Pay) {
        Log.d("updateQRInfo", "${userQR}")
        Log.d("updateQRInfo", "${pay}")
        val database = Firebase.database
        val myRef = database.getReference("UserInfo").child("userQR").child(userQR.userQR)

        Log.d("TEST", "Updating QR info in userQR node")
        // 현재 날짜를 "yyyy/MM/dd HH:mm:ss" 형식으로 반환하는 함수

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

        // userQR 노드 자체를 가져옴 (추가 child 탐색 없이)
        myRef.get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val qrData = dataSnapshot.getValue(UserQR::class.java)
                    if (qrData != null && qrData.userQR == userQR.userQR) {
                        var updatedPoints = if (pay.payName == "텀블러 인증") {
                            (qrData.userPoint ?: 0) + pay.payPrice
                        } else
                            (qrData.userPoint ?: 0) - pay.payPrice
                        var errorMsg = "정상적으로 결제되었습니다."
                        var qrUse = true
                        if (updatedPoints < 0) {
                            // 포인트 부족 예외 처리
                            errorMsg = "포인트가 부족합니다."
                            qrUse = false
                            updatedPoints = userQR.userPoint
                        } else if (qrData.payName == "" && pay.payName == "텀블러 인증(적립)") {
                            // 텀블러 적립 QR을 현장결제 리더기로 찍었을 때 예외 처리
                            errorMsg = "해당 QR은 현장결제용입니다."
                            qrUse = false
                            updatedPoints = userQR.userPoint
                        }
                        val updateData = mapOf(
                            "userId" to qrData.userId,
                            "userPoint" to updatedPoints,
                            "userQR" to qrData.userQR,
                            "payName" to pay.payName,
                            "userQrUse" to qrUse,
                            "payType" to pay.payType,
                            "paymentDate" to sdf.format(Date()),
                            "userQrError" to errorMsg,
                            "paymentPrice" to pay.payPrice,
                        )

                        // userQR 노드 직접 업데이트
                        myRef.updateChildren(updateData)
                            .addOnSuccessListener {
                                Log.d("updateDat", sdf.format(Date()))
                                Log.d("TEST", "QR 정보 업데이트 성공! 포인트: $updatedPoints")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("TEST", "QR 정보 업데이트 실패", exception)
                            }
                    } else {
                        Log.d("TEST", "QR 값이 일치하지 않음")
                    }
                } else {
                    Log.d("TEST", "userQR 노드를 찾을 수 없음")
                }
            }.addOnFailureListener { exception ->
                Log.e("TEST", "데이터 검색 실패", exception)
            }
    }

}