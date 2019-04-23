package com.sneakers.sneakerschecker.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class SneakerModel() : Parcelable {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        if (dest != null) {
            dest.writeString(this.id)
            dest.writeString(this.brand)
            dest.writeString(this.model)
            dest.writeString(this.colorway)
            dest.writeByte(if (this.limitedEdition) 1.toByte() else 0.toByte())
            dest.writeString(this.releaseDate)
            dest.writeFloat(this.size)
            dest.writeString(this.condition)
            dest.writeString(this.ownerAddress)
        }
    }

    @SerializedName("id")
    lateinit var id: String
    @SerializedName("brand")
    lateinit var brand: String
    @SerializedName("model")
    lateinit var model: String
    @SerializedName("colorway")
    lateinit var colorway: String
    @SerializedName("limitedEdition")
    var limitedEdition: Boolean = false
    @SerializedName("releaseDate")
    lateinit var releaseDate: String
    @SerializedName("size")
    var size: Float = 0.0f
    @SerializedName("condition")
    lateinit var condition: String
    @SerializedName("ownerAddress")
    lateinit var ownerAddress: String

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        brand = parcel.readString()
        model = parcel.readString()
        colorway = parcel.readString()
        limitedEdition = parcel.readByte() != 0.toByte()
        releaseDate = parcel.readString()
        size = parcel.readFloat()
        condition = parcel.readString()
        ownerAddress = parcel.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SneakerModel> {
        override fun createFromParcel(parcel: Parcel): SneakerModel {
            return SneakerModel(parcel)
        }

        override fun newArray(size: Int): Array<SneakerModel?> {
            return arrayOfNulls(size)
        }
    }
}