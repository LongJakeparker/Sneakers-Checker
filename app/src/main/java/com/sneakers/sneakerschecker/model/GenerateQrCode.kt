package com.sneakers.sneakerschecker.model

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Point
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.sneakers.sneakerschecker.constant.Constant

class GenerateQrCode {
    companion object {
        fun accountId(activity: Activity, widthPer: Double): Bitmap? {
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width = size.x

            val sharedPref = SharedPref(activity)

            val walletAddress = sharedPref.getCredentials(Constant.USER_CREDENTIALS).address

            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix = multiFormatWriter.encode(walletAddress, BarcodeFormat.QR_CODE,
                                                            (width * widthPer).toInt() ,
                                                            (width * widthPer).toInt())
                val barcodeEncoder = BarcodeEncoder()
                return barcodeEncoder.createBitmap(bitMatrix)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
            return null
        }

        fun ItemToken(activity: Activity, widthPer: Double, itemToken: String): Bitmap? {
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width = size.x

            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix = multiFormatWriter.encode(itemToken, BarcodeFormat.QR_CODE,
                    (width * widthPer).toInt() ,
                    (width * widthPer).toInt())
                val barcodeEncoder = BarcodeEncoder()
                return barcodeEncoder.createBitmap(bitMatrix)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
            return null
        }
    }
}