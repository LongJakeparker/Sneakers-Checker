package com.sneakers.sneakerschecker.model

class User : UserUpdateModel() {

    var id: Int? = 0

    var email: String? = ""

    var encryptedPrivateKey: String? = ""

    var avatar: String? = ""
}