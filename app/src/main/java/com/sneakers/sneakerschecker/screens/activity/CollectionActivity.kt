package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.collectionAdapter
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import com.sneakers.sneakerschecker.model.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_collection.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.math.BigInteger

class CollectionActivity : AppCompatActivity() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var sharedPref: SharedPref
    private lateinit var service: Retrofit
    private lateinit var contract: TrueGrailToken
    private var listCollection: ArrayList<SneakerModel> = ArrayList()

    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

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
        contract = web3?.let { Contract.getInstance(it, sharedPref.getCredentials(Constant.USER_CREDENTIALS)) }!!

        builder = AlertDialog.Builder(this)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()

        recyclerCollection.layoutManager = LinearLayoutManager(this)

        getCollection()
    }

    private fun getCollection() {
        dialog.show()
        val accessToken = "Bearer " + sharedPref.getUser(Constant.WALLET_USER).accessToken
        val userAddress = sharedPref.getCredentials(Constant.USER_CREDENTIALS).address
        val call = service.create(MainApi::class.java)
            .getCollection(accessToken, userAddress)
        call.enqueue(object : Callback<ArrayList<SneakerModel>> {
            override fun onFailure(call: Call<ArrayList<SneakerModel>>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(this@CollectionActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ArrayList<SneakerModel>>, response: Response<ArrayList<SneakerModel>>) {
                dialog.dismiss()
                if (response.code() == 200) {
                    response.body()?.forEach { item ->
                        val rxCheckOwner = contract.ownerOf(BigInteger(item.id))
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
                                    recyclerCollection.adapter =
                                        collectionAdapter(listCollection, this@CollectionActivity)
                                }
                            }

                        compositeDisposable.add(rxCheckOwner)
                    }
                } else {
                    Toast.makeText(this@CollectionActivity, response.message(), Toast.LENGTH_SHORT).show()
                }
            }

        })

    }
}
