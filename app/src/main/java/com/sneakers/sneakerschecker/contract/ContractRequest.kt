package com.sneakers.sneakerschecker.contract

import com.google.gson.Gson
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.utils.ErrorUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import one.block.eosiojava.error.rpcProvider.RpcProviderError
import one.block.eosiojavarpcprovider.error.EosioJavaRpcProviderInitializerError
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl
import org.json.JSONException
import java.util.concurrent.Callable

class ContractRequest {
    companion object {
        val nodeUrl = ""
        val compositeDisposable: CompositeDisposable = CompositeDisposable()

        fun getGrailInfoObservable(grailId: String, eosCallBack: EOSCallBack) {
            val callable = {
                val rpcProvider: EosioJavaRpcProviderImpl
                try {
                    rpcProvider = EosioJavaRpcProviderImpl(nodeUrl)
                    val getCurrentBalanceRequestJSON = "{\n" +
                            "\t\"code\" : \"eosio.token\"\n" +
                            "\t\"account\" : \"" + 21313 + "\"\n" +
                            "}"

                    val requestBody = RequestBody.create(
                        okhttp3.MediaType.parse("application/json; charset=utf-8"),
                        getCurrentBalanceRequestJSON
                    )
                    rpcProvider.getCurrencyBalance(requestBody)
                } catch (eosioJavaRpcProviderInitializerError: EosioJavaRpcProviderInitializerError) {
                    // Happens if creating EosioJavaRpcProviderImpl unsuccessful
//                    eosioJavaRpcProviderInitializerError.printStackTrace();

//                    this.publishProgress(Boolean.toString(false), eosioJavaRpcProviderInitializerError.asJsonString());
                } catch (rpcProviderError: RpcProviderError) {
                    // Happens if calling getCurrentBalance unsuccessful
//                    rpcProviderError.printStackTrace();

                    // try to get response from backend if the process fail from backend
                    val rpcResponseError = ErrorUtils.getBackendError(rpcProviderError)
                    if (rpcResponseError != null) {
                        val backendErrorMessage =
                            ErrorUtils.getBackendErrorMessageFromResponse(rpcResponseError)
//                        this.publishProgress(Boolean.toString(false), backendErrorMessage);
                        null
                    }
                    else {

                    }

//                    this.publishProgress(Boolean.toString(false), rpcProviderError.getMessage());
                } catch (e: JSONException) {
                    // Happens if parsing JSON response unsuccessful
                    e.printStackTrace()
//                    this.publishProgress(Boolean.toString(false), e.getMessage());
                }
            } as Callable<String>

            val callObservable = Observable.fromCallable(callable)
            val disposable = callObservable.subscribeOn(Schedulers.io())
                .subscribe({response ->
                    val sneakerModel =
                        Gson().fromJson(response, SneakerModel::class.java)
                    eosCallBack.onDone(sneakerModel as SneakerModel, null)
                },
                    {
                        e -> eosCallBack.onDone(null, e)
                    })

            compositeDisposable.add(disposable)
        }

        fun checkOwnerObservable(grailId: String, eosCallBack: EOSCallBack) {
            val callable = {
                val rpcProvider: EosioJavaRpcProviderImpl
                try {
                    rpcProvider = EosioJavaRpcProviderImpl(nodeUrl)
                    val getCurrentBalanceRequestJSON = "{\n" +
                            "\t\"code\" : \"eosio.token\"\n" +
                            "\t\"account\" : \"" + 21313 + "\"\n" +
                            "}"

                    val requestBody = RequestBody.create(
                        okhttp3.MediaType.parse("application/json; charset=utf-8"),
                        getCurrentBalanceRequestJSON
                    )
                    rpcProvider.getCurrencyBalance(requestBody)
                } catch (eosioJavaRpcProviderInitializerError: EosioJavaRpcProviderInitializerError) {
                    // Happens if creating EosioJavaRpcProviderImpl unsuccessful
//                    eosioJavaRpcProviderInitializerError.printStackTrace();

//                    this.publishProgress(Boolean.toString(false), eosioJavaRpcProviderInitializerError.asJsonString());
                } catch (rpcProviderError: RpcProviderError) {
                    // Happens if calling getCurrentBalance unsuccessful
//                    rpcProviderError.printStackTrace();

                    // try to get response from backend if the process fail from backend
                    val rpcResponseError = ErrorUtils.getBackendError(rpcProviderError)
                    if (rpcResponseError != null) {
                        val backendErrorMessage =
                            ErrorUtils.getBackendErrorMessageFromResponse(rpcResponseError)
//                        this.publishProgress(Boolean.toString(false), backendErrorMessage);
                        null
                    }
                    else {

                    }

//                    this.publishProgress(Boolean.toString(false), rpcProviderError.getMessage());
                } catch (e: JSONException) {
                    // Happens if parsing JSON response unsuccessful
                    e.printStackTrace();
//                    this.publishProgress(Boolean.toString(false), e.getMessage());
                }
            } as Callable<String>

            val callObservable = Observable.fromCallable(callable)
            val disposable = callObservable.subscribeOn(Schedulers.io())
                .subscribe({response ->
                    val sneakerModel =
                        Gson().fromJson(response, SneakerModel::class.java)
                    eosCallBack.onDone(sneakerModel as SneakerModel, null)
                },
                    {
                            e -> eosCallBack.onDone(null, e)
                    })

            compositeDisposable.add(disposable)
        }

        interface EOSCallBack {
            fun onDone(result: Any?, e: Throwable?)
        }
    }
}