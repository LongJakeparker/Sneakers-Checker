package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.SerializedName

class SignIn {
    @SerializedName("accessToken")
    var accessToken: String

    @SerializedName("refreshToken")
    var refreshToken: String

    @SerializedName("user")
    var user: User

    constructor(accessToken: String, refreshToken: String, user: User) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.user = user
    }
}