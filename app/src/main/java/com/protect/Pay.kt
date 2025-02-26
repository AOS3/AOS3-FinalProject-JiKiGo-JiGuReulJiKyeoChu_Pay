package com.protect

import android.os.Parcel
import android.os.Parcelable

data class Pay(
    val payName: String = "",
    val payPrice: Int = 0,
    val payType: String = "",
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(payName)
        parcel.writeInt(payPrice)
        parcel.writeString(payType)
    }

    companion object CREATOR : Parcelable.Creator<Pay> {
        override fun createFromParcel(parcel: Parcel): Pay {
            return Pay(parcel)
        }

        override fun newArray(size: Int): Array<Pay?> {
            return arrayOfNulls(size)
        }
    }

}