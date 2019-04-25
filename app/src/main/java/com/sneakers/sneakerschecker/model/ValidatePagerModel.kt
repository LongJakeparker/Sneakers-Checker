package com.sneakers.sneakerschecker.model

class ValidatePagerModel {
    var content: Int?
    var isSuccessed: Boolean? = null

    constructor(content: Int?, isSuccessed: Boolean?) {
        this.content = content
        this.isSuccessed = isSuccessed
    }
}