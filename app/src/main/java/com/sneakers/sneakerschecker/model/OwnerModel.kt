package com.sneakers.sneakerschecker.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class OwnerModel() : Parcelable {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        if (dest != null) {
            dest.writeString(this.brand)
            dest.writeString(this.physicalAddress)
            dest.writeString(this.blockchainAddress)
        }
    }

    @SerializedName("brand")
    var brand: String? = null
    @SerializedName("physicalAddress")
    var physicalAddress: String? = null
    @SerializedName("blockchainAddress")
    lateinit var blockchainAddress: String

    constructor(parcel: Parcel) : this() {
        brand = parcel.readString()
        physicalAddress = parcel.readString()
        blockchainAddress = parcel.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OwnerModel> {
        override fun createFromParcel(parcel: Parcel): OwnerModel {
            return OwnerModel(parcel)
        }

        override fun newArray(size: Int): Array<OwnerModel?> {
            return arrayOfNulls(size)
        }
    }
}