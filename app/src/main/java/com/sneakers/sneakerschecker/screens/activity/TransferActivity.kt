package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.common.hash.Hashing
import com.google.zxing.integration.android.IntentIntegrator
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import com.sneakers.sneakerschecker.model.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_transfer.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.math.BigInteger


class TransferActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var sellItem: SneakerModel
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var contract: TrueGrailToken
    private lateinit var sharedPref: SharedPref
    private lateinit var service: Retrofit
    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    companion object {
        fun start(activity: Activity, item: SneakerModel) {
            val intent = Intent(activity, TransferActivity::class.java)
            intent.putExtra(Constant.EXTRA_SNEAKER, item)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        sharedPref = SharedPref(this)

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        val web3 = Web3Instance.getInstance()
        contract = web3?.let { Contract.getInstance(it, sharedPref.getCredentials(Constant.USER_CREDENTIALS)) }!!

        if (intent.getParcelableExtra<SneakerModel>(Constant.EXTRA_SNEAKER) != null) {
            sellItem = intent.getParcelableExtra(Constant.EXTRA_SNEAKER)
        }

        builder = AlertDialog.Builder(this)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()

        loadItemInfo()
        btnBackSell.setOnClickListener(this)
        btnScanAddress.setOnClickListener(this)
        btnNextSell.setOnClickListener(this)
    }

    private fun loadItemInfo() {
        if (sellItem.limitedEdition) {
            tvIsLimited.visibility = View.VISIBLE
        }

        tvItemId.text = sellItem.id
        tvItemBrand.text = sellItem.brand
        tvItemModelName.text = sellItem.model
        tvItemColorWay.text = sellItem.colorway
        tvItemReleaseDate.text = sellItem.releaseDate
        tvItemSize.text = sellItem.size.toString()
        tvItemCondition.text = sellItem.condition
        tvItemOwnerAddress.text = sellItem.ownerAddress
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBackSell -> onBackPressed()

            R.id.btnScanAddress -> goToScan()

            R.id.btnNextSell -> commitItem()
        }
    }

    private fun commitItem() {
        dialog.show()
        sellItem.ownerAddress = etTransferAddress.text.toString().trim()
        val newHash = Hashing.sha1().hashObject(sellItem, { _, _ -> })

        val sneakerReceipt = contract.transfer(
            etTransferAddress.text.toString().trim(),
            BigInteger(sellItem.id),
            newHash.toString()
        )
            .flowable()
            .subscribeOn(Schedulers.io())
            .subscribe({ response ->
                    if (response != null) {
                        callApi()
                    }
                },
                { throwable ->
                    dialog.dismiss()
                    Log.e("TAG", "Throwable " + throwable.message)
                })

        compositeDisposable.add(sneakerReceipt)

    }

    private fun callApi() {
        val accessToken = "Bearer " + sharedPref.getUser(Constant.WALLET_USER).accessToken
        val call = service.create(MainApi::class.java!!)
            .changeOwnership(accessToken, sellItem.id, etTransferAddress.text.toString().toLowerCase())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(
                    this@TransferActivity,
                    "Failed to change ownership in database",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                dialog.dismiss()
                if (response.code() == 203) {
                    finish()
                } else {
                    Toast.makeText(
                        this@TransferActivity,
                        response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        })
    }

    private fun goToScan() {
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
        intentIntegrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                etTransferAddress.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
