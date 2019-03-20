package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.PhraseAdapter
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import com.squareup.picasso.Picasso
import android.R.attr.y
import android.R.attr.x
import android.graphics.Point
import android.view.Display





// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class PrintPhraseFragment : Fragment() {
    private lateinit var fragmentView: View

    private lateinit var recyclerPhrase: RecyclerView
    private lateinit var btnNext: Button
    private lateinit var btnPrintPhrase: Button
    private lateinit var ivQrCode: ImageView


    private lateinit var urlQrCode: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_print_phrase, container, false)

        recyclerPhrase = fragmentView!!.findViewById(R.id.layout_list_phrase_print)
        btnNext = fragmentView!!.findViewById(R.id.btnNextPrint)
        btnPrintPhrase = fragmentView!!.findViewById(R.id.btnPrintPhrasePrint)
        ivQrCode = fragmentView!!.findViewById(com.sneakers.sneakerschecker.R.id.ivQrCodePrint)

        val sharedPref = SharedPref(this.context!!)

        val walletAddress = sharedPref.getString(Constant.WALLET_ADDRESS)
//        urlQrCode = "https://chart.googleapis.com/chart?chs=300x300&cht=qr&chl=$walletAddress&choe=UTF-8"
//        Picasso.get().load(urlQrCode).into(ivQrCode)

        val display = activity!!.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x

        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(walletAddress, BarcodeFormat.QR_CODE, (width*0.4).toInt() , (width*0.4).toInt())
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            ivQrCode.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        val seed = sharedPref.getString(Constant.WALLET_MNEMONIC).split(" ")

        recyclerPhrase!!.layoutManager = GridLayoutManager(context, Constant.GRID_COLUMN)
        recyclerPhrase!!.adapter = PhraseAdapter(seed, context!!)

        return fragmentView
    }
}
