package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.ListCardAdapter
import com.sneakers.sneakerschecker.adapter.SneakerHistoryAdapter
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.ContractRequest
import com.sneakers.sneakerschecker.model.CreditCard
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SneakerHistory
import com.sneakers.sneakerschecker.model.SneakerHistoryContract
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import kotlinx.android.synthetic.main.activity_grail_history.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class GrailHistoryActivity : BaseActivity() {
    var itemId: Long? = 0
    private lateinit var service: Retrofit
    var sneakerHistoryAdapter: SneakerHistoryAdapter? = null
    var sneakerHistory = ArrayList<SneakerHistory?>()

    companion object {
        fun start(activity: Activity, itemName: String, itemId: Long) {
            val intent = Intent(activity, GrailHistoryActivity::class.java)
            intent.putExtra(Constant.EXTRA_SNEAKER_NAME, itemName)
            intent.putExtra(Constant.EXTRA_SNEAKER_ID, itemId)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = intent?.getLongExtra(Constant.EXTRA_SNEAKER_ID, 0)

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

//        getHistoryFromContract()
        sneakerHistory.add(null)
        sneakerHistory.add(null)
        sneakerHistory.add(null)
        sneakerHistory.add(null)
        sneakerHistory.add(null)
        sneakerHistory.add(null)
        sneakerHistory.add(null)
        sneakerHistory.add(null)
        sneakerHistory.add(null)
        setupRecyclerView()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_grail_history
    }

    override fun getScreenTitle(): String {
        return getString(R.string.label_grail_story, intent?.getStringExtra(Constant.EXTRA_SNEAKER_NAME))
    }

    override fun isShowBackButton(): Boolean {
        return true
    }

    private fun getHistoryFromContract() {
        ContractRequest.getTableRowObservable(ContractRequest.getQueryTertiaryIndex(Constant.CONTRACT_HISTORIES,
            itemId!!),
            object : ContractRequest.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    if (e == null) {
                        val sneakerContractModel =
                            Gson().fromJson((result as JSONArray).getJSONObject(0).toString(), SneakerHistoryContract::class.java)
                        getHistoryFromDb(sneakerContractModel)
                    } else {
                        val alertDialogFragment =
                            AlertDialogFragment.newInstance(e.localizedMessage)
                        alertDialogFragment.show(
                            supportFragmentManager,
                            AlertDialogFragment::class.java.simpleName
                        )
                    }
                }
            })
    }

    private fun getHistoryFromDb(sneakerContractModel: SneakerHistoryContract) {
        val call = service.create(MainApi::class.java)
            .getTradeHistory(sneakerContractModel)
        call.enqueue(object : Callback<SneakerHistory> {
            override fun onFailure(call: Call<SneakerHistory>, t: Throwable) {
                val alertDialogFragment =
                    AlertDialogFragment.newInstance(t.message)
                alertDialogFragment.show(
                    supportFragmentManager,
                    AlertDialogFragment::class.java.simpleName
                )
            }

            override fun onResponse(
                call: Call<SneakerHistory>,
                response: Response<SneakerHistory>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val userInfo = response.body()!!.issue!!
                    }
                } else {
                    val alertDialogFragment =
                        AlertDialogFragment.newInstance(response.message())
                    alertDialogFragment.show(
                        supportFragmentManager,
                        AlertDialogFragment::class.java.simpleName
                    )
                }
            }

        })
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        rvContent.layoutManager = layoutManager
        sneakerHistoryAdapter = SneakerHistoryAdapter(sneakerHistory)
        sneakerHistoryAdapter!!.setListener(object : SneakerHistoryAdapter.Listener {
            override fun onSelectItem(title: String) {

            }

        })
        rvContent.adapter = sneakerHistoryAdapter
    }
}
