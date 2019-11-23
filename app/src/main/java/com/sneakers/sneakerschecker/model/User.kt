package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class User {

    @SerializedName("eosName")
    @Expose
    var eosName: String

    @SerializedName("username")
    @Expose
    var username: String

    @SerializedName("email")
    @Expose
    var email: String

    @SerializedName("encryptedPrivateKey")
    @Expose
    var encryptedPrivateKey: String

    @SerializedName("role")
    @Expose
    var role: String

    @SerializedName("address")
    @Expose
    var address: String

    constructor(
        eosName: String,
        username: String,
        email: String,
        encryptedPrivateKey: String,
        role: String,
        address: String
    ) {
        this.eosName = eosName
        this.username = username
        this.email = email
        this.encryptedPrivateKey = encryptedPrivateKey
        this.role = role
        this.address = address
    }
}