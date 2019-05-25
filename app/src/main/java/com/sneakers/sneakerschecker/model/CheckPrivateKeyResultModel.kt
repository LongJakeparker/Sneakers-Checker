package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.SerializedName

class CheckPrivateKeyResultModel {
    @SerializedName("email")
    var email: String

    constructor(email: String) {
        this.email = email
    }
}