package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class UserUpdateModel {

    var userIdentity: String? = ""
    var publicKey: String? = ""
    var eosName: String? = ""
    var username: String? = ""
    var role: String? = ""
    var address: String? = ""
}