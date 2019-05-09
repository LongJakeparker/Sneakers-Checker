package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.view.animation.Interpolator
import android.widget.Toast
import com.google.common.hash.Hashing
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.ValidatePagerAdapter
import com.sneakers.sneakerschecker.animations.FixedSpeedScroller
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import com.sneakers.sneakerschecker.model.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sneaker_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.math.BigInteger


class SneakerInfoActivity : AppCompatActivity(), View.OnClickListener {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var contract: TrueGrailToken
    private lateinit var sharedPref: SharedPref
    private lateinit var service: Retrofit

    private lateinit var itemToken: String
    private lateinit var validatePagerAdapter: ValidatePagerAdapter
    private val validateSteps = ArrayList<ValidatePagerModel>()
    private lateinit var validatedItem: ValidateModel

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

        val web3 = Web3Instance.getInstance()
        contract = web3?.let { Contract.getInstance(it, sharedPref.getCredentials(Constant.USER_CREDENTIALS)) }!!

        createListPager()
        setupValidatePager()

        object : CountDownTimer(1500, 500) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                validateSteps[0].isSuccessed = true
                validatePagerAdapter.notifyDataSetChanged()
                object : CountDownTimer(500, 500) {
                    override fun onFinish() {
                        movePagerNext()
                        object : CountDownTimer(500, 500) {
                            override fun onFinish() {
                                callApi()
                            }

                            override fun onTick(millisUntilFinished: Long) {}
                        }.start()
                    }

                    override fun onTick(millisUntilFinished: Long) {}
                }.start()
            }

        }.start()

        btnDoneValidate.setOnClickListener(this)
        btnBackValidate.setOnClickListener(this)
        btnSellValidate.setOnClickListener(this)
    }

    private fun createListPager() {
        validateSteps.add(ValidatePagerModel(R.string.label_validate_step_1, null))
        validateSteps.add(ValidatePagerModel(R.string.label_validate_step_2, null))
        validateSteps.add(ValidatePagerModel(R.string.label_validate_step_3, null))
    }

    private fun setupValidatePager() {
        validatePagerAdapter = ValidatePagerAdapter(this, validateSteps)
        viewPagerValidateProgress.adapter = validatePagerAdapter
        viewPagerValidateProgress.clipToPadding = false
        viewPagerValidateProgress.pageMargin = 24

        val mScroller = ViewPager::class.java.getDeclaredField("mScroller")
        mScroller.isAccessible = true
        val interpolator = ViewPager::class.java.getDeclaredField("sInterpolator")
        interpolator.isAccessible = true

        val scroller = FixedSpeedScroller(
            this,
            interpolator.get(null) as Interpolator
        )
        mScroller.set(viewPagerValidateProgress, scroller)
    }

    private fun getItem(i: Int): Int {
        return viewPagerValidateProgress.getCurrentItem() + i
    }

    private fun movePagerNext() {
        val current = getItem(+1)
        viewPagerValidateProgress.currentItem = current
    }

    private fun callApi() {
        val call = service.create(MainApi::class.java!!)
            .validateSneaker(itemToken)
        call.enqueue(object : Callback<ValidateModel> {
            override fun onFailure(call: Call<ValidateModel>, t: Throwable) {
                validatePagerAdapter.updatePager(this@SneakerInfoActivity, 1, false)
                setTextValidateFail()
                Toast.makeText(
                    this@SneakerInfoActivity,
                    "Something went wrong when validate",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(call: Call<ValidateModel>, response: Response<ValidateModel>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val responseHash = response.body()?.hash
                        validatedItem = response.body()!!
                        val newHash = Hashing.sha1().hashObject(validatedItem, { _, _ -> })
                        validatePagerAdapter.updatePager(this@SneakerInfoActivity, 1, true)
                        object : CountDownTimer(500, 500) {
                            override fun onFinish() {
                                movePagerNext()
                                object : CountDownTimer(500, 500) {
                                    override fun onFinish() {
                                        validateItem(responseHash!!)
                                    }

                                    override fun onTick(millisUntilFinished: Long) {}
                                }.start()
                            }

                            override fun onTick(millisUntilFinished: Long) {}
                        }.start()
                    } else {
                        validatePagerAdapter.updatePager(this@SneakerInfoActivity, 1, false)
                        setTextValidateFail()
                    }
                } else {
                    validatePagerAdapter.updatePager(this@SneakerInfoActivity, 1, false)
                    setTextValidateFail()
                }
            }

        })
    }

    private fun validateItem(responseHash: String) {
        var blockchainHash: String? = ""
        val rxGetSneakerHash = contract.tokenMetadata(BigInteger(itemToken))
            .flowable()
            .subscribeOn(Schedulers.io())
            .subscribe({ response -> blockchainHash = response },
                { throwable ->
                    Log.e("TAG", "Throwable " + throwable.message)
                    validatePagerAdapter.updatePager(this, 2, false)
                    setTextValidateFail()
                },
                {
                    if (blockchainHash.isNullOrEmpty()) {
                        validatePagerAdapter.updatePager(this, 2, false)
                        setTextValidateFail()
                    } else {
                        if (responseHash.equals(blockchainHash)) {
                            validatePagerAdapter.updatePager(this@SneakerInfoActivity, 2, true)
                            loadItemInfo()
                        } else {
                            validatePagerAdapter.updatePager(this@SneakerInfoActivity, 2, false)
                            setTextValidateFail()
                        }
                    }
                })

        compositeDisposable.add(rxGetSneakerHash)
    }

    private fun loadItemInfo() {
        runOnUiThread {
            object : CountDownTimer(500, 500) {
                override fun onFinish() {
                    tvIsValidated.visibility = VISIBLE
                    layoutItemInfo.visibility = VISIBLE

                    if (validatedItem.detail.limitedEdition) {
                        tvIsLimited.visibility = VISIBLE
                    }

                    tvItemId.text = validatedItem.detail.id
                    tvItemBrand.text = validatedItem.detail.brand
                    tvItemModelName.text = validatedItem.detail.model
                    tvItemColorWay.text = validatedItem.detail.colorway
                    tvItemReleaseDate.text = validatedItem.detail.releaseDate
                    tvItemSize.text = validatedItem.detail.size.toString()
                    tvItemCondition.text = validatedItem.detail.condition
                    tvItemOwnerAddress.text = validatedItem.detail.ownerAddress

                    tvOwnerBrand.text = validatedItem.owner?.brand
                    tvOwnerPhysicalAddress.text = validatedItem.owner?.physicalAddress

                    if (validatedItem.detail.ownerAddress == sharedPref.getCredentials(Constant.USER_CREDENTIALS).address) {
                        btnSellValidate.visibility = VISIBLE
                    } else {
                        btnDoneValidate.visibility = VISIBLE
                    }
                }

                override fun onTick(millisUntilFinished: Long) {}
            }.start()
        }
    }

    private fun setTextValidateFail() {
        runOnUiThread {
            object : CountDownTimer(500, 500) {
                override fun onFinish() {
                    tvIsValidated.setText(R.string.label_is_not_validated)
                    tvIsValidated.setTextColor(resources.getColor(R.color.colorMain))
                    tvIsValidated.visibility = VISIBLE
                    btnDoneValidate.visibility = VISIBLE
                }

                override fun onTick(millisUntilFinished: Long) {}
            }.start()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDoneValidate, R.id.btnBackValidate -> onBackPressed()
            R.id.btnSellValidate -> TransferActivity.start(this@SneakerInfoActivity, validatedItem.detail)
        }
    }
}
