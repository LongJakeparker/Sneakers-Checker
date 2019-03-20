package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SignIn {
    @SerializedName("accessToken")
    @Expose
    var accessToken: String

    @SerializedName("refreshToken")
    @Expose
    var refreshToken: String

    @SerializedName("user")
    @Expose
    var user: User

    constructor(accessToken: String, refreshToken: String, user: User) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.user = user
    }
}