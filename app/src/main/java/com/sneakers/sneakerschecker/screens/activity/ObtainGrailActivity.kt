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
    var sneakerObtained: ValidateModel? = null
    var sneakerSale: SneakerModel? = null
    var isObtained: Boolean = false

    companion object {
        fun start(activity: Activity, sneaker: ValidateModel) {
            val intent = Intent(activity, ObtainGrailActivity::class.java)
            intent.putExtra(Constant.EXTRA_VALIDATE_SNEAKER, sneaker)
            intent.putExtra(Constant.EXTRA_IS_OBTAINED, true)
            activity.startActivity(intent)
        }

        fun start(activity: Activity, sneaker: SneakerModel) {
            val intent = Intent(activity, ObtainGrailActivity::class.java)
            intent.putExtra(Constant.EXTRA_VALIDATE_SNEAKER, sneaker)
            intent.putExtra(Constant.EXTRA_IS_OBTAINED, false)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_obtain_grail)

        isObtained = intent?.getBooleanExtra(Constant.EXTRA_IS_OBTAINED, false)!!

        if (isObtained) {
            sneakerObtained = intent?.getSerializableExtra(Constant.EXTRA_VALIDATE_SNEAKER) as ValidateModel
            bindObtainedData()
        } else {
            sneakerSale = intent?.getSerializableExtra(Constant.EXTRA_VALIDATE_SNEAKER) as SneakerModel
            bindSaleData()
        }

        btnDone.setOnClickListener{ finish() }
    }

    private fun bindObtainedData() {
        tvItemName.text = sneakerObtained?.detail?.model
        tvItemBrand.text = sneakerObtained?.detail?.brand
        tvItemSize.text = sneakerObtained?.detail?.size.toString()
        tvItemReleaseDate.text = sneakerObtained?.detail?.releaseDate
    }

    private fun bindSaleData() {
        tvItemName.text = sneakerSale?.model
        tvItemBrand.text = sneakerSale?.brand
        tvItemSize.text = sneakerSale?.size.toString()
        tvItemReleaseDate.text = sneakerSale?.releaseDate

        tvCongrats.text = getString(R.string.text_successful)
        tvDesCongrats.text = getString(R.string.text_des_tranfer_successful)
    }
}
