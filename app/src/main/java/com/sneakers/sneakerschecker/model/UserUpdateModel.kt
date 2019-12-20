package com.sneakers.sneakerschecker.model

import java.io.Serializable

open class UserUpdateModel : Serializable {

    var userIdentity: String? = ""
    var publicKey: String? = ""
    var eosName: String? = ""
    var username: String? = ""
    var role: String? = ""
    var address: String? = ""
    var dob: String? = ""
    var gender: String? = ""
}