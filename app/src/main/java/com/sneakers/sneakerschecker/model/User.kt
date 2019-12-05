package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class User : UserUpdateModel {

    @SerializedName("id")
    @Expose
    var id: Int

    @SerializedName("email")
    @Expose
    var email: String

    @SerializedName("encryptedPrivateKey")
    @Expose
    var encryptedPrivateKey: String

    constructor(
        id: Int,
        email: String,
        encryptedPrivateKey: String,
        userIdentity: String,
        publicKey: String,
        eosName: String,
        username: String,
        role: String,
        address: String
    ) : super(userIdentity, publicKey, eosName, username, role, address) {
        this.id = id
        this.email = email
        this.encryptedPrivateKey = encryptedPrivateKey
    }
}