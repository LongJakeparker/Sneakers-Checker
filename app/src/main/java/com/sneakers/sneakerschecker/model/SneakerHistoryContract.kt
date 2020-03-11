package com.sneakers.sneakerschecker.model

data class SneakerHistoryContract(
    val id: Long = 0,
    val trx_id: String? = null,
    val sneaker_id: String? = null,
    val claim_account: String? = null,
    val buyer_id: Long? = null,
    val seller_id: Long? = null,
    val transaction_type: String? = null,
    val created_at: String? = null
)