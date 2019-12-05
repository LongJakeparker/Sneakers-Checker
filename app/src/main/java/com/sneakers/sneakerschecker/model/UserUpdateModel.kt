package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class UserUpdateModel {

    @SerializedName("userIdentity")
    @Expose
    var userIdentity: String

    @SerializedName("publicKey")
    @Expose
    var publicKey: String

    @SerializedName("eosName")
    @Expose
    var eosName: String

    @SerializedName("username")
    @Expose
    var username: String


    @SerializedName("role")
    @Expose
    var role: String

    @SerializedName("address")
    @Expose
    var address: String

    constructor(
        userIdentity: String,
        publicKey: String,
        eosName: String,
        username: String,
        role: String,
        address: String
    ) {
        this.userIdentity = userIdentity
        this.publicKey = publicKey
        this.eosName = eosName
        this.username = username
        this.role = role
        this.address = address
    }
}