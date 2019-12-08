package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.CollectionAdapter
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.ContractRequest
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_collection.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.Type


class CollectionActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPref
    private lateinit var service: Retrofit
    private var listCollection: ArrayList<SneakerModel> = ArrayList()
    private var listContractCollection: ArrayList<SneakerContractModel> = ArrayList()
    private var userInfo: User? = null

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

        userInfo = sharedPref.getUser(Constant.LOGIN_USER)?.user

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

//        listCollection.add(SneakerModel())
//        listCollection.add(SneakerModel())
//        listCollection.add(SneakerModel())
//
//        viewPagerCollection.adapter = CollectionAdapter(listCollection, this)
//        viewPagerCollection.addOnPageChangeListener(viewChangeListener)

        getCollectionFromContract()

        btnBack.setOnClickListener { onBackPressed() }
    }

    private fun getCollectionFromContract() {
        CommonUtils.toggleLoading(window.decorView.rootView, true)
        ContractRequest.getTableRowObservable(Constant.CONTRACT_TABLE_SNEAKER,
            userInfo?.id?.toLong()!!,
            true,
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    val type: Type =
                        object : TypeToken<List<SneakerContractModel?>?>() {}.type
                    listContractCollection = Gson().fromJson((result as JSONArray).toString(), type)

                    getCollection()
                }
            })
    }

    private fun getCollection() {
        val accessToken = "Bearer " + sharedPref.getUser(Constant.LOGIN_USER)?.accessToken
        val param = HashMap<String, Any>()
        param["sneakerIdList"] = getListSneakerId()
        val call = service.create(MainApi::class.java)
            .getCollection(accessToken, userInfo?.id!!, param)
        call.enqueue(object : Callback<CollectionModel> {
            override fun onFailure(call: Call<CollectionModel>, t: Throwable) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                Toast.makeText(this@CollectionActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<CollectionModel>,
                response: Response<CollectionModel>
            ) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                if (response.isSuccessful) {
                    listCollection.clear()
                    listCollection.addAll(response.body()?.collection!!)
                    viewPagerCollection.adapter = CollectionAdapter(listCollection, this@CollectionActivity)
                    viewPagerCollection.addOnPageChangeListener(viewChangeListener)
                    if (listCollection.size > 0) {
                        btnScan.visibility = View.VISIBLE
                        rlBtnSaleAndStolen.visibility = View.VISIBLE
                    } else {
                        llViewNoData.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@CollectionActivity, response.message(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        })

    }

    private fun getListSneakerId(): java.util.ArrayList<Long> {
        val listId = ArrayList<Long>()
        for (i in 0 until listContractCollection.size) {
            listId.add(listContractCollection[i].id!!)
        }

        return listId
    }

    val viewChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {

        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }
}
