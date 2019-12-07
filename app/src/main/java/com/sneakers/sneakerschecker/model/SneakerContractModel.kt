package com.sneakers.sneakerschecker.model

import java.io.Serializable

class SneakerContractModel : Serializable{
    var id: Long? = 0
    var info_hash: String? = ""
    var owner_id: Int? = 0
    var owner: String? = ""
    var status: String? = ""

}