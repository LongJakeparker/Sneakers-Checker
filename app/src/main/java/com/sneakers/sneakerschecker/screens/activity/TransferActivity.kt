package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.common.hash.Hashing
import com.google.zxing.integration.android.IntentIntegrator
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import com.sneakers.sneakerschecker.model.Contract
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.model.Web3Instance
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_transfer.*
import org.web3j.protocol.admin.Admin
import org.web3j.protocol.http.HttpService
import java.math.BigInteger


class TransferActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var sellItem: SneakerModel
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var contract: TrueGrailToken
    private lateinit var sharedPref: SharedPref

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

        val web3 = Web3Instance.getInstance()
        contract = web3?.let { Contract.getInstance(it, sharedPref.getString(Constant.ACCOUNT_ID)) }!!

        if (intent.getParcelableExtra<SneakerModel>(Constant.EXTRA_SNEAKER) != null) {
            sellItem = intent.getParcelableExtra(Constant.EXTRA_SNEAKER)
        }

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
        sellItem.ownerAddress = etTransferAddress.text.toString()
        val newHash = Hashing.sha1().hashObject(sellItem, { _, _ -> })
        val admin = Admin.build(HttpService(Constant.ETHEREUM_API_URL))
        val personalUnlockAccount = admin.personalUnlockAccount(sharedPref.getString(Constant.ACCOUNT_ID), "@Aa1234567")
            .flowable()
            .subscribeOn(Schedulers.io())
            .subscribe({ response ->
                if (response.accountUnlocked()) {
                    val sneakerReceipt = contract.transfer(
                        etTransferAddress.text.toString(),
                        BigInteger(sellItem.id),
                        newHash.toString()
                    )
                        .flowable()
                        .subscribeOn(Schedulers.io())
                        .subscribe({ response -> val transferEvent = response },
                            { throwable ->
                                Log.e("TAG", "Throwable " + throwable.message)
                            },
                            {
                                Toast.makeText(this, "Sneaker was transferred", Toast.LENGTH_LONG).show()
                            })

                    compositeDisposable.add(sneakerReceipt)
                }
            },
                { throwable ->
                    Log.e("TAG", "Throwable " + throwable.message)
                })

        compositeDisposable.add(personalUnlockAccount)
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
