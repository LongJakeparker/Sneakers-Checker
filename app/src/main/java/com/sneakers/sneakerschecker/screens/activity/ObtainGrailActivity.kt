package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.model.ValidateModel
import kotlinx.android.synthetic.main.activity_obtain_grail.*

class ObtainGrailActivity : AppCompatActivity() {
    var sneaker: ValidateModel? = null

    companion object {
        fun start(activity: Activity, sneaker: ValidateModel) {
            val intent = Intent(activity, ObtainGrailActivity::class.java)
            intent.putExtra(Constant.EXTRA_VALIDATE_SNEAKER, sneaker)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_obtain_grail)

        sneaker = intent?.getSerializableExtra(Constant.EXTRA_VALIDATE_SNEAKER) as ValidateModel

        bindData()

        btnDone.setOnClickListener{ finish() }
    }

    private fun bindData() {
        tvItemName.text = sneaker?.detail?.model
        tvItemBrand.text = sneaker?.detail?.brand
        tvItemSize.text = sneaker?.detail?.size.toString()
        tvItemReleaseDate.text = sneaker?.detail?.releaseDate
    }
}
