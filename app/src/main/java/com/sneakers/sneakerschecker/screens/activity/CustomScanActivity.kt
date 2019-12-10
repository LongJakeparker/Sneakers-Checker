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
import androidx.fragment.app.Fragment
import com.google.common.hash.Hashing
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.`interface`.IDialogListener
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.ContractRequest
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.utils.CommonUtils
import com.sneakers.sneakerschecker.screens.fragment.ConfirmDialogFragment
import kotlinx.android.synthetic.main.activity_custom_scan.*
import kotlinx.android.synthetic.main.include_bottom_view_scan.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.nio.charset.StandardCharsets


class CustomScanActivity : AppCompatActivity(), View.OnClickListener {
    class ScanType {
        companion object {
            val SCAN_GRAIL = 1
            val SCAN_EOS_NAME = 2
            val SCAN_CLAIM_TOKEN = 3
        }
    }

    private val TAG = CustomScanActivity::class.java.simpleName
    private val MY_PERMISSIONS_REQUEST_CAMERA = 101
    private val REQUEST_CODE_CLAIM = 1005
    private val REQUEST_CODE_START_LOGIN_ACTIVITY = 1009
    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    private var lastText: String? = null
    private var scanType: Int? = null
    private var hadScanResult: Boolean = false
    private var isExpanded: Boolean = false
    private lateinit var validatedItem: ValidateModel
    private lateinit var sneakerContractModel: SneakerContractModel
    private lateinit var userInfo: User

    private var scanResult: String = ""

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
            lastText = result.text

            when (scanType) {

                ScanType.SCAN_EOS_NAME -> {
                    if (scanResult.length == 12) {
                        val returnIntent = Intent()
                        returnIntent.putExtra(Constant.EXTRA_USER_EOS_NAME, scanResult)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    } else {
                        barcodeView?.pause()
                        val confirmDialogFragment = ConfirmDialogFragment.newInstance(resources.getString(R.string.dialog_title_invalid_eos_name),
                            resources.getString(R.string.dialog_msg_invalid_eos_name), true)
                        confirmDialogFragment.setListener(object : IDialogListener {
                            override fun onDialogFinish(tag: String, ok: Boolean, result: Bundle) {
                                if (ok) {
                                    barcodeView?.resume()
                                    lastText = ""
                                }
                            }
                            override fun onDialogCancel(tag: String) {

                            }
                        })
                        confirmDialogFragment.show(supportFragmentManager, ConfirmDialogFragment::class.java.simpleName)
                    }
                }

                ScanType.SCAN_GRAIL -> scanGrail()

                ScanType.SCAN_CLAIM_TOKEN -> scanClaimToken()
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

        fun startForResult(
            activity: Activity,
            sneakerContractModel: SneakerContractModel,
            validateItem: ValidateModel,
            scanType: Int,
            requestCode: Int
        ) {
            val intent = Intent(activity, CustomScanActivity::class.java)
            intent.putExtra(Constant.EXTRA_SNEAKER, sneakerContractModel)
            intent.putExtra(Constant.EXTRA_VALIDATE_SNEAKER, validateItem)
            intent.putExtra(Constant.EXTRA_SCAN_TYPE, scanType)
            activity.startActivityForResult(intent, requestCode)
        }

        fun startForResult(fragment: Fragment, scanType: Int, requestCode: Int) {
            val intent = Intent(fragment.context, CustomScanActivity::class.java)
            intent.putExtra(Constant.EXTRA_SCAN_TYPE, scanType)
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_custom_scan)

        scanType = intent?.getIntExtra(Constant.EXTRA_SCAN_TYPE, 0)

        if (intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) != null) {
            sneakerContractModel =
                intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) as SneakerContractModel
            validatedItem =
                intent?.getSerializableExtra(Constant.EXTRA_VALIDATE_SNEAKER) as ValidateModel
        }

        sharedPref = SharedPref(this)

        service = RetrofitClientInstance().getRetrofitInstance()!!

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
        tvClaim.setOnClickListener(this)
    }

    private fun setupBottomView() {
        when (scanType) {
            ScanType.SCAN_GRAIL -> {
                setContentBottomView(
                    R.string.activity_title_scan_grail, R.string.header_guide_scan_grail,
                    R.drawable.ic_guideline_scan_grail, R.string.title_guide_scan_grail,
                    R.string.content_guide_scan_grail
                )
            }

            ScanType.SCAN_CLAIM_TOKEN -> {
                userInfo = sharedPref.getUser(Constant.LOGIN_USER)?.user!!
                setContentBottomView(
                    R.string.activity_title_scan_claim,
                    R.string.header_guide_scan_claim,
                    R.drawable.ic_guideline_scan_claim,
                    R.string.title_guide_scan_claim,
                    R.string.content_guide_scan_claim
                )
            }

            ScanType.SCAN_EOS_NAME -> {
                setContentBottomView(
                    R.string.activity_title_scan_eos_name,
                    R.string.header_guide_scan_eos_name,
                    R.drawable.ic_guideline_scan_eos_name,
                    R.string.title_guide_scan_eos_name,
                    R.string.content_guide_scan_eos_name
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
        when (v) {
            rlRootBottomView -> {
                if (!isExpanded) {
                    expandBottomView()
                }
            }
            btnCloseBottomView, blurView -> expandBottomView()

            btnClose -> finish()

            tvClaim -> {
                if (CommonUtils.isNonLoginUser(this@CustomScanActivity)) {
                    val intent = Intent(this@CustomScanActivity, LoginActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE_START_LOGIN_ACTIVITY)
                } else {
                    startForResult(
                        this,
                        sneakerContractModel,
                        validatedItem,
                        ScanType.SCAN_CLAIM_TOKEN,
                        REQUEST_CODE_CLAIM
                    )
                }
            }

            tvSale -> GrailsTradingActivity.start(this, validatedItem.detail!!)
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
                llItemStatus.visibility = View.GONE
                tvSale.visibility = View.GONE
                tvClaim.visibility = View.GONE
                tvItemOwner.visibility = View.GONE
                llItemStolen.visibility = View.GONE

                when (sneakerContractModel.status) {
                    Constant.ItemCondition.NEW -> {
                        llItemStatus.visibility = View.VISIBLE
                        setItemStatus(R.drawable.ic_no_owner, R.string.text_item_is_new)
                        tvClaim.visibility = View.VISIBLE
                    }

                    Constant.ItemCondition.NOT_NEW -> {
                        llItemStatus.visibility = View.VISIBLE
                        if (!CommonUtils.isNonLoginUser(this) &&
                            sneakerContractModel.owner_id == sharedPref.getUser(Constant.LOGIN_USER)?.user?.id) {
                            setItemStatus(R.drawable.ic_is_your, R.string.text_item_is_your)
                            tvSale.visibility = View.VISIBLE
                        } else {
                            setItemStatus(R.drawable.ic_own_by, R.string.text_item_own_by)
                            tvItemOwner.visibility = View.VISIBLE
                            tvItemOwner.text = userInfo.username
                        }
                    }
                }

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

    private fun setItemStatus(icon: Int, label: Int) {
        tvItemStatus.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        tvItemStatus.text = getString(label)
    }

    private fun scanClaimToken() {
        startProgress()
        val jsonData = ContractRequest.transferSneakerJson(
            sneakerContractModel.id!!,
            userInfo.id!!
        )

        ContractRequest.callEosApi(sneakerContractModel.owner!!,
            ContractRequest.METHOD_TRANSFER,
            jsonData,
            null,
            null,
            scanResult,
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    if (e == null) {
                        Toast.makeText(
                            this@CustomScanActivity,
                            "Transaction id: $result",
                            Toast.LENGTH_LONG
                        ).show()
                        ObtainGrailActivity.start(this@CustomScanActivity, validatedItem)

                        val returnIntent = Intent()
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    } else {
                        Toast.makeText(this@CustomScanActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    private fun startProgress() {
        lastText = ""
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
    }

    private fun scanGrail() {
        startProgress()
        startVerifySneaker()
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

    private fun startVerifySneaker() {
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
                if (response.isSuccessful) {
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
                    if (response.code() == 404) {
                        showMessageScanFail(
                            this@CustomScanActivity.resources.getString(R.string.msg_sneaker_not_exist),
                            true
                        )
                    } else {
                        showMessageScanFail(
                            this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                            true
                        )
                    }
                }
            }

        })
    }

    private fun validateItem(responseHash: String) {
        ContractRequest.getTableRowObservable(Constant.CONTRACT_TABLE_SNEAKER,
            scanResult.toLong(),
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    if (e == null) {
                        sneakerContractModel =
                            Gson().fromJson((result as JSONArray).getJSONObject(0).toString(), SneakerContractModel::class.java)
                        val blockchainHash = sneakerContractModel.info_hash
                        if (blockchainHash.isNullOrEmpty()) {
                            showMessageScanFail(
                                this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                                true
                            )
                        } else {
                            if (responseHash == blockchainHash) {
                                validateFactory()
                            } else {
                                showMessageScanFail(
                                    "Sneaker information not match with blockchain",
                                    true
                                )
                            }
                        }
                    } else {
                        Log.d("Scan error: ", e.localizedMessage)
                    }
                }
            })
    }

    private fun validateFactory() {
        val gson =
            GsonBuilder().registerTypeAdapter(
                FactoryModel::class.java,
                FactoryModelJsonSerializer()
            )
                .create()
        val strResponseHash = gson.toJson(validatedItem.factory)
        val responseHash =
            Hashing.sha256().hashString(strResponseHash, StandardCharsets.UTF_8)
                .toString()

        ContractRequest.getTableRowObservable(Constant.CONTRACT_TABLE_USERS,
            validatedItem.detail?.factoryId!!.toLong(),
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    if (e == null) {
                        val factoryContractModel =
                            Gson().fromJson((result as JSONArray).getJSONObject(0).toString(), UserContractModel::class.java)
                        val blockchainHash = factoryContractModel.info_hash
                        if (blockchainHash.isNullOrEmpty()) {
                            showMessageScanFail(
                                this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                                true
                            )
                        } else {
                            if (responseHash == blockchainHash) {
                                if (sneakerContractModel.owner_id != 0) {
                                    getUserInfor()
                                } else {
                                    loadItemInfo()
                                }
                            } else {
                                showMessageScanFail(
                                    "Factory information not match with blockchain",
                                    true
                                )
                            }
                        }
                    } else {
                        Log.d("Scan error: ", e.localizedMessage)
                    }
                }
            })
    }

    private fun getUserInfor() {
        val call = service.create(MainApi::class.java)
            .getUserInformation(sneakerContractModel.owner_id!!)
        call.enqueue(object : Callback<CollectorModel> {
            override fun onFailure(call: Call<CollectorModel>, t: Throwable) {
                showMessageScanFail(
                    this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                    true
                )
            }

            override fun onResponse(
                call: Call<CollectorModel>,
                response: Response<CollectorModel>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        userInfo = response.body()!!.collector!!

                        val hashUserModel = UserUpdateModel()
                        hashUserModel.userIdentity = userInfo.userIdentity
                        hashUserModel.username = userInfo.username
                        hashUserModel.eosName = userInfo.eosName
                        hashUserModel.publicKey = userInfo.publicKey
                        hashUserModel.role = userInfo.role
                        hashUserModel.address = userInfo.address

                        val gson =
                            GsonBuilder().registerTypeAdapter(
                                UserUpdateModel::class.java,
                                UserModelJsonSerializer()
                            )
                                .create()
                        val strResponseHash = gson.toJson(hashUserModel)
                        val responseHash =
                            Hashing.sha256().hashString(strResponseHash, StandardCharsets.UTF_8)
                                .toString()

                        validateUser(responseHash)
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

    private fun validateUser(responseHash: String) {
        ContractRequest.getTableRowObservable(Constant.CONTRACT_TABLE_USERS,
            sneakerContractModel.owner_id!!.toLong(),
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    if (e == null) {
                        val userContractModel =
                            Gson().fromJson((result as JSONArray).getJSONObject(0).toString(), UserContractModel::class.java)
                        val blockchainHash = userContractModel.info_hash
                        if (blockchainHash.isNullOrEmpty()) {
                            showMessageScanFail(
                                this@CustomScanActivity.resources.getString(R.string.msg_scan_fail),
                                true
                            )
                        } else {
                            if (responseHash == blockchainHash) {
                                loadItemInfo()
                            } else {
                                showMessageScanFail(
                                    "Owner information not match with blockchain",
                                    true
                                )
                            }
                        }
                    } else {
                        Log.d("Scan error: ", e.localizedMessage)
                    }
                }
            })
    }

    private fun loadItemInfo() {
        hadScanResult = true
        barcodeView?.resume()

        tvItemName.text = validatedItem.detail?.model
        tvItemBrand.text = validatedItem.detail?.brand
        tvItemSize.text = validatedItem.detail?.size.toString()
        tvItemReleaseDate.text = validatedItem.detail?.releaseDate

        progressBar.visibility = View.GONE
        rlScanFail.visibility = View.GONE

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == REQUEST_CODE_CLAIM &&
                    resultCode == Activity.RESULT_OK -> {
                finish()
            }
        }
    }
}
