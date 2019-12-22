package com.sneakers.sneakerschecker.model

import java.io.Serializable
import java.util.*

open class UserUpdateModel : Serializable {

    var userIdentity: String? = ""
    var publicKey: String? = ""
    var eosName: String? = ""
    var username: String? = ""
    var role: String? = ""
    var address: String? = ""
    var dob: Date? = null
    var gender: String? = ""
}