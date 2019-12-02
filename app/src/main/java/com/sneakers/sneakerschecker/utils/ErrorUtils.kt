package com.sneakers.sneakerschecker.utils


import one.block.eosiojava.error.EosioError
import one.block.eosiojava.models.rpcProvider.response.RPCResponseError
import one.block.eosiojavarpcprovider.error.EosioJavaRpcProviderCallError

object ErrorUtils {

    /**
     * Recursively look for a specific error inside causes loop of an EosioError
     *
     * @param errorClass - the error class to find
     * @param error      - the error object to search
     * @param <T>        - the generic class which extends from EosioError
     * @return the error which class is specified by input. Return null if could not find the specific class.
    </T> */
    fun <T : Exception> getErrorObject(errorClass: Class<*>, error: Exception): T? {
        if (error.javaClass == errorClass) {
            return error as T
        }

        return if (error.cause == null) {
            null
        } else getErrorObject(errorClass, error.cause as Exception)

        // Recursively look deeper
    }

    /**
     * Recursively look for the error message of a specific error inside causes loop of an EosioError
     *
     * @param errorClass - the error class to get the message
     * @param error      - the error object to search
     * @return the error message which class is specified by input. Return the root cause message if could not find the specific class.
     */
    fun getError(errorClass: Class<*>, error: EosioError): String {
        return if (error.javaClass == errorClass || error.cause == null) {
            error.message!!
        } else getError(errorClass, error.cause as EosioError)

    }

    /**
     * Get backend error class [RPCResponseError] if an backend error is available
     *
     * @param error the error class to get the backend error
     * @return [RPCResponseError] object. Return null if input error does not contain any backend error.
     */
    fun getBackendError(error: EosioError): RPCResponseError? {
        val rpcError = ErrorUtils.getErrorObject<EosioJavaRpcProviderCallError>(
            EosioJavaRpcProviderCallError::class.java,
            error
        )
        return rpcError?.rpcResponseError

    }

    /**
     * Format and return a back end error message from a [RPCResponseError] object
     *
     * @param error the backend error
     * @return Formatted backend error message from input
     */
    fun getBackendErrorMessageFromResponse(error: RPCResponseError): String {
        val detail = StringBuilder()
        if (!error.error.details.isEmpty()) {
            for (errorDetail in error.error.details) {
                detail.append(errorDetail.message).append(" - ")
            }
        }

        return error.message + " - Code: " + error.error.code + " - What " + error.error.code + " - detail: " + detail.toString()
    }
}