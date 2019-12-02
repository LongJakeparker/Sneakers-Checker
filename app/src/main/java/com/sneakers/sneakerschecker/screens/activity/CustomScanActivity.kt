package com.sneakers.sneakerschecker.screens.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.hash.Hashing
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.Contract
import com.sneakers.sneakerschecker.contract.TrueGrailToken
import com.sneakers.sneakerschecker.model.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_custom_scan.*
import kotlinx.android.synthetic.main.include_bottom_view_scan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.math.BigInteger
import java.nio.charset.StandardCharsets


class CustomScanActivity : AppCompatActivity(), View.OnClickListener {
    class ScanType {
        companion object {
            val SCAN_GRAIL = 1
            val SCAN_PRIVATE_KEY = 2
            val SCAN_ADDRESS = 3
            val SCAN_CLAIM_TOKEN = 4
        }
    }

    private val TAG = CustomScanActivity::class.java.simpleName
    private val MY_PERMISSIONS_REQUEST_CAMERA = 101
    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    private var lastText: String? = null
    private var scanType: Int? = null
    private var hadScanResult: Boolean = false
    private var isExpanded: Boolean = false
    private lateinit var validatedItem: ValidateModel
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var scanResult: String = ""

    private lateinit var contract: TrueGrailToken
    private lateinit var sharedPref: SharedPref
    private lateinit var service: Retrofit

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }

            scanResult = result.text
            beepManager!!.playBeepSoundAndVibrate()

            when (scanType) {
                ScanType.SCAN_PRIVATE_KEY -> {
                    val returnIntent = Intent()
                    returnIntent.putExtra(Constant.EXTRA_PRIVATE_KEY, scanResult)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }

                ScanType.SCAN_GRAIL -> scanGrail()
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    companion object {
        fun start(activity: Activity, scanType: Int) {
            val intent = Intent(activity, CustomScanActivity::class.java)
            intent.putExtra(Constant.EXTRA_SCAN_TYPE, scanType)
            activity.startActivity(intent)
        }

        fun startForResult(activity: Activity, scanType: Int, requestCode: Int) {
            val intent = Intent(activity, CustomScanActivity::class.java)
            intent.putExtra(Constant.EXTRA_SCAN_TYPE, scanType)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_custom_scan)

        scanType = intent?.getIntExtra(Constant.EXTRA_SCAN_TYPE, 0)

        sharedPref = SharedPref(this)

        barcodeView = findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView
        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView!!.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView!!.initializeFromIntent(intent)
        barcodeView!!.decodeContinuous(callback)

        beepManager = BeepManager(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA
            )

            Toast.makeText(
                this,
                resources.getText(R.string.msg_grant_permission_camera),
                Toast.LENGTH_LONG
            ).show()

        }

        setupBottomView()

        rlRootBottomView.setOnClickListener(this)
        btnCloseBottomView.setOnClickListener(this)
        blurView.setOnClickListener(this)
        btnClose.setOnClickListener(this)
    }

    private fun setupBottomView() {
        when (scanType) {
            ScanType.SCAN_GRAIL -> {
                setContentBottomView(
                    R.string.activity_title_scan_grail, R.string.header_guide_scan_grail,
                    R.drawable.ic_guideline_scan_grail, R.string.title_guide_scan_grail,
                    R.string.content_guide_scan_grail
                )

                //Get instant retrofit
                service = RetrofitClientInstance().getRetrofitInstance()!!

                val web3 = Web3Instance.getInstance()

//                if (CommonUtils.isNonLoginUser(this)) {
//                    contract = web3?.let {
//                        Contract.getInstance(
//                            it,
//                            sharedPref.getCredentials(Constant.APP_CREDENTIALS)
//                        )
//                    }!!
//                } else {
//                    contract = web3?.let {
//                        Contract.getInstance(
//                            it,
//                            sharedPref.getCredentials(Constant.USER_CREDENTIALS)
//                        )
//                    }!!
//                }

                contract = web3?.let {
                    Contract.getInstance(
                        it,
                        sharedPref.getCredentials(Constant.APP_CREDENTIALS)
                    )
                }!!
            }

            ScanType.SCAN_PRIVATE_KEY -> {
                setContentBottomView(
                    R.string.activity_title_scan_private_key,
                    R.string.header_guide_scan_private_key,
                    R.drawable.ic_guideline_scan_private_key,
                    R.string.title_guide_scan_private_key,
                    R.string.content_guide_scan_private_key
                )
            }
        }
    }

    private fun setContentBottomView(
        activityTitle: Int,
        header: Int,
        image: Int,
        title: Int,
        content: Int
    ) {
        tvTitle.text = resources.getText(activityTitle)
        tvHeaderGuide.text = resources.getText(header)
        ivGuideImage.setImageResource(image)
        tvTitleGuide.text = resources.getText(title)
        tvContentGuide.text = resources.getText(content)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rlRootBottomView -> {
                if (!isExpanded) {
                    expandBottomView()
                }
            }
            R.id.btnCloseBottomView, R.id.blurView -> expandBottomView()

            R.id.btnClose -> finish()
        }
    }

    private fun expandBottomView() {
        if (!hadScanResult) {
            if (progressBar.visibility == View.GONE) {
                if (!isExpanded) {
                    tvHeaderGuide.visibility = View.GONE

                    rlRootGuide.visibility = View.VISIBLE
                    ivGuideImage.visibility = View.VISIBLE

                    blurView.visibility = View.VISIBLE
                    blurView.isClickable = true
                    blurView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_view))

                    barcodeView?.pause()
                    isExpanded = true
                } else {
                    tvHeaderGuide.visibility = View.VISIBLE

                    rlRootGuide.visibility = View.GONE
                    ivGuideImage.visibility = View.GONE

                    blurView.visibility = View.GONE
                    blurView.isClickable = false
                    blurView.startAnimation(
                        AnimationUtils.loadAnimation(
                            this,
                            R.anim.fade_out_view
                        )
                    )

                    barcodeView?.resume()
                    isExpanded = false
                }
            }
        } else {
            if (!isExpanded) {
                llScanResultDetail.visibility = View.VISIBLE

                blurView.visibility = View.VISIBLE
                blurView.isClickable = true
                blurView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_view))

                barcodeView?.pause()
                isExpanded = true
            } else {
                llScanResultDetail.visibility = View.GONE

                blurView.visibility = View.GONE
                blurView.isClickable = false
                blurView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_view))

                barcodeView?.resume()
                isExpanded = false
            }
        }
    }

    private fun scanGrail() {
        progressBar.visibility = View.VISIBLE
        tvHeaderGuide.visibility = View.GONE

        rlScanResultHeader.visibility = View.GONE
        llScanResultDetail.visibility = View.GONE
        showMessageScanFail(false)

        blurView.visibility = View.GONE
        blurView.isClickable = false
        blurView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_view))

        barcodeView?.pause()
        isExpanded = false

        callApi()
    }

    private fun showMessageScanFail(isShow: Boolean) {
        showMessageScanFail("", isShow)
    }

    private fun showMessageScanFail(msg: String, isShow: Boolean) {
        if (isShow) {
            if (msg.isNotEmpty()) {
                tvScanFailMessage.text = msg
            }

            progressBar.visibility = View.GONE
            rlScanFail.visibility = View.VISIBLE
            rlRootBottomView.isClickable = false

            barcodeView?.resume()
        } else {
            rlScanFail.visibility = View.GONE
            rlRootBottomView.isClickable = true
        }
    }

    private fun callApi() {
        val call = service.create(MainApi::class.java)
            .validateSneaker(scanResult)
        call.enqueue(object : Callback<ValidateModel> {
            override fun onFailure(call: Call<ValidateModel>, t: Throwable) {
                showMessageScanFail(
                    this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                    true
                )
            }

            override fun onResponse(call: Call<ValidateModel>, response: Response<ValidateModel>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        validatedItem = response.body()!!

                        val gson =
                            GsonBuilder().registerTypeAdapter(
                                SneakerModel::class.java,
                                SneakerModelJsonSerializer()
                            )
                                .create()
                        val strResponseHash = gson.toJson(validatedItem.detail)
                        val responseHash =
                            Hashing.sha256().hashString(strResponseHash, StandardCharsets.UTF_8)
                                .toString()

                        validateItem(responseHash)
                    } else {
                        showMessageScanFail(
                            this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                            true
                        )
                    }
                } else {
                    showMessageScanFail(
                        this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                        true
                    )
                }
            }

        })
    }

    private fun validateItem(responseHash: String) {
        var blockchainHash: String? = ""
        val rxGetSneakerHash = contract.tokenMetadata(BigInteger(scanResult))
            .flowable()
            .subscribeOn(Schedulers.io())
            .subscribe({ response -> blockchainHash = response },
                { throwable ->
                    Log.e("TAG", "Throwable " + throwable.message)
                }
            ) {
                if (blockchainHash.isNullOrEmpty()) {
                    showMessageScanFail(
                        this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                        true
                    )
                } else {
                    if (responseHash == blockchainHash) {
                        loadItemInfo()
                    } else {
                        showMessageScanFail("Validate with blockchain failed", true)
                    }
                }
            }

        compositeDisposable.add(rxGetSneakerHash)
    }

    private fun loadItemInfo() {
        hadScanResult = true
        barcodeView?.resume()

        tvItemName.text = validatedItem.detail.model
        tvItemBrand.text = validatedItem.detail.brand
        tvItemSize.text = validatedItem.detail.size.toString()
        tvItemReleaseDate.text = validatedItem.detail.releaseDate

        progressBar.visibility = View.GONE

        rlScanResultHeader.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        barcodeView?.resume()
    }

    override fun onPause() {
        super.onPause()

        barcodeView?.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
