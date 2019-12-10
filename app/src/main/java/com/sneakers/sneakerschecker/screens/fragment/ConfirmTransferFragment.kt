package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.ContractRequest
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.model.User
import kotlinx.android.synthetic.main.fragment_confirm_transfer.*
import kotlinx.android.synthetic.main.fragment_create_transfer.ivLimited
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailBrand
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailName
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailReleaseDate
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailSize

class ConfirmTransferFragment : Fragment(), View.OnClickListener {
    private var fragmentView: View? = null
    private var sneaker: SneakerModel? = null
    private var receiverEosName: String? = null
    private var receiverName: String? = null
    private var currentUser: User? = null

    private lateinit var sharedPref: SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_confirm_transfer, container, false)

        sharedPref = context?.let { SharedPref(it) }!!
        currentUser = sharedPref.getUser(Constant.LOGIN_USER)?.user

        sneaker = activity?.intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) as SneakerModel
        receiverName = arguments?.getString(Constant.EXTRA_RECEIVER_NAME)
        receiverEosName = arguments?.getString(Constant.EXTRA_RECEIVER_EOS_NAME)


        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSneakerInfo()
        loadReceiverInfo()

        btnBack.setOnClickListener(this)
        btnConfirm.setOnClickListener(this)
    }

    private fun loadSneakerInfo() {
        tvGrailName.text = sneaker?.model
        tvGrailBrand.text = sneaker?.brand
        tvGrailSize.text = sneaker?.size.toString()
        tvGrailReleaseDate.text = sneaker?.releaseDate

        if (sneaker?.limitedEdition!!) {
            ivLimited.visibility = View.VISIBLE
        }
    }

    private fun loadReceiverInfo() {
        tvReceiverName.text = receiverName
        tvReceiverEosName.text = receiverEosName
    }

    override fun onClick(v: View?) {
        when (v) {
            btnBack -> activity?.onBackPressed()

            btnConfirm -> createTransaction()
        }
    }

    private fun createTransaction() {
        val jsonData = ContractRequest.transferSneakerJson(
            sneaker?.id!!,
            currentUser?.id!!
        )

        ContractRequest.callEosApi(currentUser?.eosName!!,
            ContractRequest.METHOD_TRANSFER,
            jsonData,
            null,
            currentUser?.encryptedPrivateKey,
            null,
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {

                }

            })
    }
}