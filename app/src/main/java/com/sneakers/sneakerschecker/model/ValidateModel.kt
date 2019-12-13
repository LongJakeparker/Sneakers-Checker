package com.sneakers.sneakerschecker.model

import java.io.Serializable

class ValidateModel : Serializable {
    var detail: SneakerModel? = null
    var factory: FactoryContractModel? = null
//    override fun describeContents(): Int {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        super.writeToParcel(parcel, flags)
//        parcel.writeString(this.hash)
//    }
//
//    @SerializedName("hash")
//    var hash: String
//
//    constructor(parcel: Parcel) : super(parcel) {
//        hash = parcel.readString()
//    }
//
//    companion object CREATOR : Parcelable.Creator<ValidateModel> {
//        override fun createFromParcel(parcel: Parcel): ValidateModel {
//            return ValidateModel(parcel)
//        }
//
//        override fun newArray(size: Int): Array<ValidateModel?> {
//            return arrayOfNulls(size)
//        }
//    }
}