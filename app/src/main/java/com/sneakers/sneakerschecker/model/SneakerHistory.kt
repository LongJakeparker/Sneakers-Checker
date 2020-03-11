package com.sneakers.sneakerschecker.model

data class SneakerHistory(
    val id: Int = 0,
    val createdAt: String = "",
    val sellerName: String? = null,
    val buyerName: String? = null,
    val factoryName: String? = null,
    val type: String? = null
) {
    companion object {
        const val TYPE_ISSUE = "issue"
        const val TYPE_CLAIM = "claim"
        const val TYPE_RESELL = "resell"
    }
}