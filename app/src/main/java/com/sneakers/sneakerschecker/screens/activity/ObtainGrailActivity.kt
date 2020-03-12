package com.sneakers.sneakerschecker.screens.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.FinishTransferEvent
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.model.ValidateModel
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_obtain_grail.*
import org.greenrobot.eventbus.EventBus

class ObtainGrailActivity : AppCompatActivity() {
    var sneakerValidateModel: ValidateModel? = null
    var sneakerModel: SneakerModel? = null
    var isObtained: Boolean = false
    var isSneakerModel: Boolean = false

    companion object {
        fun start(context: Context, sneaker: ValidateModel) {
            val intent = Intent(context, ObtainGrailActivity::class.java)
            intent.putExtra(Constant.EXTRA_VALIDATE_SNEAKER, sneaker)
            intent.putExtra(Constant.EXTRA_IS_OBTAINED, true)
            context.startActivity(intent)
        }

        fun start(context: Context, sneaker: SneakerModel) {
            val intent = Intent(context, ObtainGrailActivity::class.java)
            intent.putExtra(Constant.EXTRA_VALIDATE_SNEAKER, sneaker)
            intent.putExtra(Constant.EXTRA_IS_OBTAINED, false)
            intent.putExtra(Constant.EXTRA_IS_SNEAKER_MODEL, true)
            context.startActivity(intent)
        }

        fun start(context: Context, sneaker: SneakerModel, isObtained: Boolean) {
            val intent = Intent(context, ObtainGrailActivity::class.java)
            intent.putExtra(Constant.EXTRA_VALIDATE_SNEAKER, sneaker)
            intent.putExtra(Constant.EXTRA_IS_OBTAINED, isObtained)
            intent.putExtra(Constant.EXTRA_IS_SNEAKER_MODEL, true)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_obtain_grail)

        isObtained = intent?.getBooleanExtra(Constant.EXTRA_IS_OBTAINED, false)!!
        isSneakerModel = intent?.getBooleanExtra(Constant.EXTRA_IS_SNEAKER_MODEL, false)!!

        if (isObtained) {
            if (isSneakerModel) {
                sneakerModel = intent?.getSerializableExtra(Constant.EXTRA_VALIDATE_SNEAKER) as SneakerModel
            } else {
                sneakerValidateModel = intent?.getSerializableExtra(Constant.EXTRA_VALIDATE_SNEAKER) as ValidateModel
            }
            bindData()
        } else {
            sneakerModel = intent?.getSerializableExtra(Constant.EXTRA_VALIDATE_SNEAKER) as SneakerModel
            bindData()
            tvCongrats.text = getString(R.string.text_successful)
            tvDesCongrats.text = getString(R.string.text_des_tranfer_successful)
        }

        btnDone.setOnClickListener{
            EventBus.getDefault().post(FinishTransferEvent())
            finish()
        }
    }

    private fun bindData() {
        if (isSneakerModel) {
            ivBrandLogo.setImageResource(CommonUtils.getBrandLogo(sneakerModel?.brand!!))
            tvItemName.text = sneakerModel?.model
            tvItemBrand.text = sneakerModel?.brand
            tvItemSize.text = sneakerModel?.size.toString()
            tvItemReleaseDate.text = sneakerModel?.releaseDate
        } else {
            ivBrandLogo.setImageResource(CommonUtils.getBrandLogo(sneakerValidateModel?.detail?.brand!!))
            tvItemName.text = sneakerValidateModel?.detail?.model
            tvItemBrand.text = sneakerValidateModel?.detail?.brand
            tvItemSize.text = sneakerValidateModel?.detail?.size.toString()
            tvItemReleaseDate.text = sneakerValidateModel?.detail?.releaseDate
        }
    }
}
