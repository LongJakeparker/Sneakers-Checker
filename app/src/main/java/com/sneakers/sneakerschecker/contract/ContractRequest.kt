package com.sneakers.sneakerschecker.contract

import android.content.Context
import android.util.Log
import com.sneakers.sneakerschecker.model.AESCrypt
import com.sneakers.sneakerschecker.utils.ErrorUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import one.block.eosiojava.error.session.TransactionPrepareError
import one.block.eosiojava.error.session.TransactionSignAndBroadCastError
import one.block.eosiojava.implementations.ABIProviderImpl
import one.block.eosiojava.models.rpcProvider.Action
import one.block.eosiojava.models.rpcProvider.Authorization
import one.block.eosiojava.models.rpcProvider.response.PushTransactionResponse
import one.block.eosiojava.session.TransactionSession
import one.block.eosiojavaabieosserializationprovider.AbiEosSerializationProviderImpl
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl
import one.block.eosiosoftkeysignatureprovider.SoftKeySignatureProviderImpl
import one.block.eosiosoftkeysignatureprovider.error.ImportKeyError
import java.util.concurrent.Callable

class ContractRequest {
    companion object {
        val nodeUrl = "https://api.jungle.alohaeos.com:443"
        val contractName = "truegrail123"
        val compositeDisposable: CompositeDisposable = CompositeDisposable()

        val METHOD_UPDATE_USER = "updateuser"

        fun updateUserJson(eosName: String, id: Int, infoHash: String): String {
            return "{\n" +
                    "\"user\": \"" + eosName + "\",\n" +
                    "\"user_id\": " + id + ",\n" +
                    "\"user_info_hash\": \"" + infoHash + "\"\n" +
                    "}"
        }

        fun callEosApi(
            context: Context,
            eosName: String,
            methodName: String,
            jsonData: String,
            password: String?,
            encryptedPrivateKey: String?,
            eosCallBack: EOSCallBack
        ) {
            val callable = Callable<PushTransactionResponse> {
                // Creating serialization provider
                val serializationProvider = AbiEosSerializationProviderImpl()

                // Creating RPC Provider
                val rpcProvider = EosioJavaRpcProviderImpl(nodeUrl)

                // Creating ABI provider
                val abiProvider = ABIProviderImpl(rpcProvider, serializationProvider)

                // Creating Signature provider
                val signatureProvider = SoftKeySignatureProviderImpl()
                signatureProvider.isReturnLegacyFormatForK1 = true

                if (password != null) {
                    //                    val privateKey = AESCrypt.decrypt("KnyeLg5YZKe5cmRV+Yn/1BQmECdsrf7R1zEGJGhDF7Ck/RAinKxCPuvqhsg/xKgKNjOzdfEyS1xf\n" +
                    //                            "vg2hj2A4RQ==", password)
                    val privateKey = AESCrypt.decrypt(encryptedPrivateKey!!, password)
                    try {
                        signatureProvider.importKey(privateKey)
                    } catch (importKeyError: ImportKeyError) {
                        importKeyError.printStackTrace()
                        //                    this.publishProgress(java.lang.Boolean.toString(false), importKeyError.message)
                        null
                    }
                }


                // Creating TransactionProcess
                val session = TransactionSession(
                    serializationProvider,
                    rpcProvider,
                    abiProvider,
                    signatureProvider
                )
                val processor = session.transactionProcessor

                // Creating action with action's data, eosio.token contract and transfer action.
                val action = Action(
                    contractName,
                    methodName,
                    listOf(Authorization(eosName, "active")),
                    jsonData
                )
                try {

                    //                    // Prepare transaction with above action. A transaction can be executed with multiple action.
                    //                    this.publishProgress("Preparing Transaction...")
                    processor.prepare(listOf(action))
                    //
                    //                    // Sign and broadcast the transaction.
                    //                    this.publishProgress("Signing and Broadcasting Transaction...")
                    processor.signAndBroadcast()

                    //                    this.publishProgress(
                    //                        java.lang.Boolean.toString(true),
                    //                        "Finished!  Your transaction id is:  " + response.transactionId
                    //                    )
                } catch (transactionPrepareError: TransactionPrepareError) {
                    // Happens if preparing transaction unsuccessful
                    Log.d("TransactionPrepareError: ", transactionPrepareError.localizedMessage)
                    null
                    //                    this.publishProgress(
                    //                        java.lang.Boolean.toString(false),
                    //                        transactionPrepareError.localizedMessage
                    //                    )
                } catch (transactionSignAndBroadCastError: TransactionSignAndBroadCastError) {
                    // Happens if Sign transaction or broadcast transaction unsuccessful.
                    transactionSignAndBroadCastError.printStackTrace()

                    // try to get backend error if the error come from backend
                    val rpcResponseError =
                        ErrorUtils.getBackendError(transactionSignAndBroadCastError)
                    if (rpcResponseError != null) {
                        val backendErrorMessage =
                            ErrorUtils.getBackendErrorMessageFromResponse(rpcResponseError)
                        //                        this.publishProgress(java.lang.Boolean.toString(false), backendErrorMessage)
                        Log.d("rpcResponseError: ", backendErrorMessage)
                        null
                    } else {
                        null
                    }

                    //                    this.publishProgress(
                    //                        java.lang.Boolean.toString(false),
                    //                        transactionSignAndBroadCastError.message
                    //                    )
                }
            }

            if (callable != null) {
                val callObservable = Observable.fromCallable(callable)
                val disposable = callObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        eosCallBack.onDone(response.transactionId, null)
                    },
                        { e ->
                            eosCallBack.onDone(null, e)
                        })

                compositeDisposable.add(disposable)
            }
        }

        interface EOSCallBack {
            fun onDone(result: Any?, e: Throwable?)
        }
    }
}