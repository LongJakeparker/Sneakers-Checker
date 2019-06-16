package com.sneakers.sneakerschecker.model

import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.RawTransactionManager
import java.math.BigInteger

class Contract private constructor(web3: Web3j, address: String) {

    companion object {
        private val GAS_PRICE: BigInteger = BigInteger.valueOf(20000000000L)
        private val GAS_LIMIT: BigInteger = BigInteger.valueOf(6721975L)
        var sleepDuration = 15 * 1000
        var attempts = 8

        private var instance : TrueGrailToken? = null
        private var transactionManager : RawTransactionManager? = null

        fun getInstance(web3: Web3j, credentials: Credentials?): TrueGrailToken {
            if (instance == null)  // NOT thread safe!
                transactionManager = RawTransactionManager(web3, credentials, attempts, sleepDuration)
                instance = TrueGrailToken.load(
                        Constant.CONTRACT_ADDRESS, web3, transactionManager, GAS_PRICE, GAS_LIMIT)

            return instance!!
        }
    }
}