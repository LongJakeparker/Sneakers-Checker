package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import com.sneakers.sneakerschecker.model.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sneaker_info.*
import org.web3j.crypto.WalletUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.math.BigInteger

class SneakerInfoActivity : AppCompatActivity() {
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var contract: TrueGrailToken
    private lateinit var credentials: org.web3j.crypto.Credentials
    private lateinit var sharedPref: SharedPref
    private lateinit var service: Retrofit

    private lateinit var itemToken: String

    companion object {
        fun start(activity: Activity, itemToken: String) {
            val intent = Intent(activity, SneakerInfoActivity::class.java)
            intent.putExtra(Constant.EXTRA_SNEAKER_TOKEN, itemToken)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sneaker_info)

        sharedPref = SharedPref(this)

        itemToken = intent.getStringExtra(Constant.EXTRA_SNEAKER_TOKEN)

        val qrCode = GenerateQrCode.ItemToken(this, 0.6, itemToken)

        if (qrCode != null) {
            ivQrCodeValidate.setImageBitmap(qrCode)
        }

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        val web3 = Web3Instant.getInstance()
        credentials = WalletUtils.loadBip39Credentials(
            sharedPref.getString(Constant.WALLET_PASSPHRASE),
            sharedPref.getString(Constant.WALLET_MNEMONIC)
        )
        contract = Contract.getInstance(web3, credentials)

        validateItem()
    }

    private fun validateItem() {
        var blockchainHash: String? = ""
        val rxGetSneakerHash = contract.tokenMetadata(BigInteger(itemToken))
            .flowable()
            .subscribeOn(Schedulers.io())
            .subscribe({ response -> blockchainHash = response },
                { throwable -> Log.e("TAG", "Throwable " + throwable.message) },
                {
                    val call = service.create(MainApi::class.java!!)
                        .validateSneaker(itemToken)
                    call.enqueue(object : Callback<ValidateModel> {
                        override fun onFailure(call: Call<ValidateModel>, t: Throwable) {
                            Toast.makeText(
                                this@SneakerInfoActivity,
                                "Something went wrong when validate",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onResponse(call: Call<ValidateModel>, response: Response<ValidateModel>) {
                            if (response.code() == 200) {
                                val responseHash = response.body()?.hash
                                if (responseHash.equals(blockchainHash)) {
                                    //sharedPref.getString(Constant.WALLET_ADDRESS).equals(response.body()?.owner?.blockchainAddress)
                                    Toast.makeText(this@SneakerInfoActivity, "Sneaker is validated", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Toast.makeText(
                                        this@SneakerInfoActivity,
                                        "Sneaker not validated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                    })
                })

        compositeDisposable.add(rxGetSneakerHash)
    }
}
