package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.PhraseAdapter
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class CreatePhraseFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters

    private var fragmentView: View? = null
    private lateinit var recyclerPhrase: RecyclerView
    private lateinit var btnNext: Button
    private lateinit var btnPrintPhrase: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_phrase, container, false)

        recyclerPhrase = fragmentView!!.findViewById(R.id.layout_list_phrase)
        btnNext = fragmentView!!.findViewById(R.id.btnNextCreatePhrase)
        btnPrintPhrase = fragmentView!!.findViewById(R.id.btnPrintPhraseCreate)

        val sharedPref = SharedPref(this.context!!)
        val seed = sharedPref.getString(Constant.WALLET_MNEMONIC).split(" ")

        recyclerPhrase!!.layoutManager = GridLayoutManager(context, Constant.GRID_COLUMN)
        recyclerPhrase!!.adapter = PhraseAdapter(seed, context!!)

        btnNext.setOnClickListener(this)
        btnPrintPhrase.setOnClickListener(this)

        return fragmentView
    }

    override fun onClick(v: View?) {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        when (v!!.id) {
            R.id.btnNextCreatePhrase -> {
                transaction.replace(R.id.authentication_layout, ConfirmPhraseFragment())
                    .addToBackStack(null)
                    .commit()
            }

            R.id.btnPrintPhraseCreate -> {
                transaction.replace(R.id.authentication_layout, PrintPhraseFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
