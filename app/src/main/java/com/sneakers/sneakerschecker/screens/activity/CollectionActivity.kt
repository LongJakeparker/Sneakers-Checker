package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.CollectionAdapter
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.Contract
import com.sneakers.sneakerschecker.contract.TrueGrailToken
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.utils.CommonUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_collection.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class CollectionActivity : AppCompatActivity() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var sharedPref: SharedPref
    private lateinit var service: Retrofit
    private lateinit var contract: TrueGrailToken
    private var listCollection: ArrayList<SneakerModel> = ArrayList()

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, CollectionActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        sharedPref = SharedPref(this)

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        val web3 = Web3Instance.getInstance()
        contract = web3?.let {
            Contract.getInstance(
                it,
                sharedPref.getCredentials(Constant.APP_CREDENTIALS)
            )
        }!!

        listCollection.add(SneakerModel())
        listCollection.add(SneakerModel())
        listCollection.add(SneakerModel())

        viewPagerCollection.adapter = CollectionAdapter(listCollection, this)

//        getCollection()

        btnBack.setOnClickListener { onBackPressed() }
    }

    private fun getCollection() {
        CommonUtils.toggleLoading(window.decorView.rootView, true)
        val accessToken = "Bearer " + sharedPref.getUser(Constant.LOGIN_USER)?.accessToken
        val userAddress = sharedPref.getCredentials(Constant.USER_CREDENTIALS).address
        val call = service.create(MainApi::class.java)
            .getCollection(accessToken, userAddress)
        call.enqueue(object : Callback<ArrayList<SneakerModel>> {
            override fun onFailure(call: Call<ArrayList<SneakerModel>>, t: Throwable) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                Toast.makeText(this@CollectionActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<ArrayList<SneakerModel>>,
                response: Response<ArrayList<SneakerModel>>
            ) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                if (response.code() == 200) {
                    response.body()?.forEach { item ->
                        val rxCheckOwner = contract.ownerOf(item.id)
                            .flowable()
                            .subscribeOn(Schedulers.io())
                            .subscribe({ response ->
                                if (response == sharedPref.getCredentials(Constant.USER_CREDENTIALS).address) {
                                    listCollection.add(item)
                                }
                            },
                                { throwable ->
                                    Log.e("TAG", "Throwable " + throwable.message)
                                })
                            {
                                runOnUiThread {
                                    viewPagerCollection.adapter =
                                        CollectionAdapter(listCollection, this@CollectionActivity)
                                }
                            }

                        compositeDisposable.add(rxCheckOwner)
                    }
                } else {
                    Toast.makeText(this@CollectionActivity, response.message(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        })

    }
}
