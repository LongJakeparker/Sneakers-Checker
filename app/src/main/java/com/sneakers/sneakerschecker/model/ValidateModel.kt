package com.sneakers.sneakerschecker.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class ValidateModel() : Parcelable {
    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        if (dest != null) {
            dest.writeParcelable(this.detail, flags)
            dest.writeString(this.hash)
            dest.writeParcelable(this.owner, flags)
        }
    }

    @SerializedName("detail")
    lateinit var detail: SneakerModel
    @SerializedName("hash")
    lateinit var hash: String
    @SerializedName("owner")
    lateinit var owner: OwnerModel

    constructor(parcel: Parcel) : this() {
        detail = parcel.readParcelable(SneakerModel::class.java.classLoader)
        hash = parcel.readString()
        owner = parcel.readParcelable(OwnerModel::class.java.classLoader)
    }

    companion object CREATOR : Parcelable.Creator<ValidateModel> {
        override fun createFromParcel(parcel: Parcel): ValidateModel {
            return ValidateModel(parcel)
        }

        override fun newArray(size: Int): Array<ValidateModel?> {
            return arrayOfNulls(size)
        }
    }
}