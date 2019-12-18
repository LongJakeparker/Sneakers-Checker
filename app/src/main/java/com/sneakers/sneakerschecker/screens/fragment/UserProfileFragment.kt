package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_update_user_register.*

class UserProfileFragment: UpdateUserRegisterFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ibBack.visibility = View.VISIBLE

        ibBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v) {
            ibBack -> activity?.onBackPressed()
        }
    }
}