package com.sneakers.sneakerschecker.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class SellItemModel : Serializable {

//    @SerializedName("detail")
//    var detail: SneakerModel
//    @SerializedName("owner")
//    var owner: OwnerModel? = null

//    constructor(parcel: Parcel) {
//        detail = parcel.readParcelable(SneakerModel::class.java.classLoader)
//        owner = parcel.readParcelable(OwnerModel::class.java.classLoader)
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeParcelable(detail, flags)
//        parcel.writeParcelable(owner, flags)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<SellItemModel> {
//        override fun createFromParcel(parcel: Parcel): SellItemModel {
//            return SellItemModel(parcel)
//        }
//
//        override fun newArray(size: Int): Array<SellItemModel?> {
//            return arrayOfNulls(size)
//        }
//    }
}