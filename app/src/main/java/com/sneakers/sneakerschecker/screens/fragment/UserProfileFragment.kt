package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.common.hash.Hashing
import com.google.gson.GsonBuilder
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.model.CollectorModel
import com.sneakers.sneakerschecker.model.User
import com.sneakers.sneakerschecker.model.UserModelJsonSerializer
import com.sneakers.sneakerschecker.model.UserUpdateModel
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_update_user_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets

class UserProfileFragment: UpdateUserRegisterFragment() {
    private var userProfile: User? = null

    override fun isShowBackButton(): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = super.onCreateView(inflater, container, savedInstanceState)
        getUserInfor()

        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvBirthday.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnUpdate.isEnabled = validateData()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    private fun getUserInfor() {
        CommonUtils.toggleLoadingFullBg(fragmentView, true)
        val call = service.create(MainApi::class.java)
            .getUserInformation(CommonUtils.getCurrentUser(context!!)?.user?.id!!)
        call.enqueue(object : Callback<CollectorModel> {
            override fun onFailure(call: Call<CollectorModel>, t: Throwable) {
                CommonUtils.toggleLoadingFullBg(fragmentView, false)
                val alertDialogFragment =
                    AlertDialogFragment.newInstance(t.message)
                alertDialogFragment.show(
                    fragmentManager!!,
                    AlertDialogFragment::class.java.simpleName
                )
            }

            override fun onResponse(
                call: Call<CollectorModel>,
                response: Response<CollectorModel>
            ) {
                CommonUtils.toggleLoadingFullBg(fragmentView, false)
                if (response.isSuccessful) {
                    userProfile = response.body()!!.collector!!

                    loadUserInfo()
                } else {
                    val alertDialogFragment =
                        AlertDialogFragment.newInstance(response.message())
                    alertDialogFragment.show(
                        fragmentManager!!,
                        AlertDialogFragment::class.java.simpleName
                    )
                }
            }

        })
    }

    private fun loadUserInfo() {
        etUserName.setText(userProfile?.username)
        etUserEmail.setText(userProfile?.email)
        etUserAddress.setText(userProfile?.address)

        if (userProfile?.dob != null) {
            tvBirthday.text = CommonUtils.formatDate(userProfile?.dob!!)
        }

        if (userProfile?.gender == GENDER_FEMALE) {
            tvGender.text = getString(R.string.text_gender_lady)
            tvGender.setTextColor(resources.getColor(R.color.salmon, null))
            gender = GENDER_FEMALE
            rbtnLady.isChecked = true
        } else {
            tvGender.text = getString(R.string.text_gender_gentleman)
            tvGender.setTextColor(resources.getColor(R.color.brightBlue, null))
            gender = GENDER_MALE
            rbtnMen.isChecked = true
        }

        btnUpdate.isEnabled = false
    }
}