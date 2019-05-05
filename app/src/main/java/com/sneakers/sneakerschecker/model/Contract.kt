package com.sneakers.sneakerschecker.model

import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import org.web3j.protocol.Web3j
import org.web3j.tx.ClientTransactionManager
import java.math.BigInteger

class Contract private constructor(web3: Web3j, address: String) {

    companion object {
        private val GAS_PRICE: BigInteger = BigInteger.valueOf(20000000000L)
        private val GAS_LIMIT: BigInteger = BigInteger.valueOf(6721975L)

        private var instance : TrueGrailToken? = null
        private var transactionManager : ClientTransactionManager? = null

        fun getInstance(web3: Web3j, address: String): TrueGrailToken {
            if (instance == null)  // NOT thread safe!
                transactionManager = ClientTransactionManager(web3, address)
                instance = TrueGrailToken.load(
                        Constant.CONTRACT_ADDRESS, web3, transactionManager, GAS_PRICE, GAS_LIMIT)

            return instance!!
        }
    }
}