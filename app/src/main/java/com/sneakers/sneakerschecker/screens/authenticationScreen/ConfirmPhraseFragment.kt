package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.ConfirmPhraseAdapter
import com.sneakers.sneakerschecker.adapter.PhraseAdapter
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.interfaces.ConfirmPhraseCallBack
import com.sneakers.sneakerschecker.model.SharedPref
import java.util.*
import android.content.Intent
import com.sneakers.sneakerschecker.MainActivity


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class ConfirmPhraseFragment : Fragment(), ConfirmPhraseCallBack {

    private var fragmentView: View? = null
    private lateinit var recyclerPhrase: RecyclerView
    private lateinit var seed: List<String>
    private lateinit var shuffedSeed: List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_confirm_phrase, container, false)

        recyclerPhrase = fragmentView!!.findViewById(R.id.layout_list_phrase_confirm)

        val sharedPref = SharedPref(this.context!!)
        val mnemonic = sharedPref.getString(Constant.WALLET_MNEMONIC)
        seed = mnemonic.split(" ")

        shuffedSeed = mnemonic.split(" ")

        Collections.shuffle(shuffedSeed)

        recyclerPhrase!!.layoutManager = GridLayoutManager(context, Constant.GRID_COLUMN)
        recyclerPhrase!!.adapter = ConfirmPhraseAdapter(shuffedSeed, context!!, this)

        return fragmentView
    }

    override fun ClickCallBack(word: String) {
        if (word == seed[8]) {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity!!.finish()
        }
        else {
            Collections.shuffle(shuffedSeed)
            recyclerPhrase!!.adapter!!.notifyDataSetChanged()
        }
    }
}
