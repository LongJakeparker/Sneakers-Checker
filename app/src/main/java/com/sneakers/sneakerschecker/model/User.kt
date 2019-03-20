package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class User {

    @SerializedName("firstName")
    @Expose
    var firstName: String

    @SerializedName("lastName")
    @Expose
    var lastName: String

    @SerializedName("email")
    @Expose
    var email: String

    constructor(firstName: String, lastName: String, email: String) {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }
}