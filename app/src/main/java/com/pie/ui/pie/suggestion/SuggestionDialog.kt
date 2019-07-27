package com.pie.ui.pie.suggestion

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager

import com.pie.R
import com.pie.model.Suggestion
import com.pie.utils.AppConstant
import com.pie.utils.AppConstant.Companion.SUGGESTION_LIST
import kotlinx.android.synthetic.main.fragment_suggestion_dialog.*

class SuggestionDialog : DialogFragment() ,View.OnClickListener{


    private var suggestionList: ArrayList<Suggestion>? = null
    private lateinit var suggestionAdpter:Suggetionadapter

    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            suggestionList = it.getSerializable(SUGGESTION_LIST) as ArrayList<Suggestion>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_suggestion_dialog, container, false)
        init()
        clickListener()

    }

    private fun init(){
        rvSuggestion.layoutManager = LinearLayoutManager(context)
        suggestionAdpter = Suggetionadapter(R.layout.suggestion_raw_layout, this)
        rvSuggestion.adapter = suggestionAdpter
        udateList()


    }

    private fun udateList() {
        /*if(suggestionList?.size!!>0) {
            suggestionAdpter.addAll(suggestionList)
        }else{

        }*/
    }

    override fun onClick(p0: View?) {

    }
    private fun clickListener(){


    }


  /*  companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SuggestionDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
}
