package com.sneakers.sneakerschecker.model

import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import java.math.BigInteger

class Contract private constructor(web3: Web3j, credentials: Credentials) {

    companion object {
        private val GAS_PRICE: BigInteger = BigInteger.valueOf(20000000000L)
        private val GAS_LIMIT: BigInteger = BigInteger.valueOf(6721975L)

        private var instance : TrueGrailToken? = null

        fun getInstance(web3: Web3j, credentials: Credentials): TrueGrailToken {
            if (instance == null)  // NOT thread safe!
                instance = TrueGrailToken.load(
                    Constant.CONTRACT_ADDRESS, web3, credentials, GAS_PRICE, GAS_LIMIT
                )

            return instance!!
        }
    }
}