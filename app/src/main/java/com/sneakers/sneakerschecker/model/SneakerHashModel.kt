package com.sneakers.sneakerschecker.model

import java.io.Serializable

class SneakerHashModel : Serializable {
    var id: Long? = 0
    var factoryId: Int? = 0
    var brand: String? = ""
    var model: String? = ""
    var size: String? = ""
    var colorway: String? = ""
    var limitedEdition: Boolean? = false
    var releaseDate: String? = ""
}