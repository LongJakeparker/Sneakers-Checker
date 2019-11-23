package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.SerializedName

class SignUp {
    @SerializedName("eosName")
    var eosName: String

    constructor(eosName: String) {
        this.eosName = eosName
    }
}