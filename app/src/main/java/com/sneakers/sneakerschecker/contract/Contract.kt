package com.sneakers.sneakerschecker.contract

import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.RawTransactionManager
import java.math.BigInteger

class Contract private constructor(web3: Web3j, address: String) {

    companion object {
        const val CONTRACT_ADDRESS = "0xc482C0Ab3fAa90821c7a01d6D22df2DBcf82bA83"
        private val GAS_PRICE: BigInteger = BigInteger.valueOf(20000000000L)
        private val GAS_LIMIT: BigInteger = BigInteger.valueOf(6721975L)
        var sleepDuration = 15 * 1000
        var attempts = 20

        private var instance: TrueGrailToken? = null
        private var transactionManager: RawTransactionManager? = null

        fun getInstance(web3: Web3j, credentials: Credentials?): TrueGrailToken {
            if (instance == null)  // NOT thread safe!
                transactionManager =
                    RawTransactionManager(web3, credentials,
                        attempts,
                        sleepDuration
                    )
                instance = TrueGrailToken.load(
                    CONTRACT_ADDRESS, web3,
                    transactionManager,
                    GAS_PRICE,
                    GAS_LIMIT
            )

            return instance!!
        }
    }
}