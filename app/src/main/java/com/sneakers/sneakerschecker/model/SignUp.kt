package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SignUp {
    @SerializedName("passwordHash")
    @Expose
    var passwordHash: String

    constructor(passwordHash: String) {
        this.passwordHash = passwordHash
    }
}