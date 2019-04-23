package com.sneakers.sneakerschecker.model

import org.web3j.protocol.Web3j

class Web3Instant private constructor() {

    companion object {

        private var instance: Web3j? = null

        fun getInstance(): Web3j {
            return instance!!
        }

        fun setInstance(web3: Web3j) {
            instance = web3
        }
    }
}