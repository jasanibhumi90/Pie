package com.pie.ui.profile.piemate

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.pie.R
import com.pie.model.Piemate
import com.pie.ui.base.BaseActivity
import com.pie.utils.AppConstant.Companion.ARG_PIE_MATE_LIST
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.activity_my_piemates.*
import kotlinx.android.synthetic.main.toolbar_common.*

class MyPiematesActivity : BaseActivity(),View.OnClickListener {
    private lateinit var pieMatesAdapter:PiematesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_piemates)
        tvTitle.text=getString(R.string.mypiemates)
        if(intent.hasExtra(ARG_PIE_MATE_LIST)) {
            init(intent.getSerializableExtra(ARG_PIE_MATE_LIST) as ArrayList<Piemate>)
            clickListener()
        }
    }

    private fun clickListener() {
        ivBack.setOnClickListener(this)
    }

    private fun init(piemateList: ArrayList<Piemate>) {
        pieMatesAdapter = PiematesAdapter(R.layout.like_row_layout, this)
        val linearLayoutManager = LinearLayoutManager(this)
        rvPiemates.run {
            layoutManager = linearLayoutManager
            adapter=pieMatesAdapter
            if(piemateList.size>0)
                pieMatesAdapter.addAll(piemateList)
            else
                tvnoDataFound.visibility=View.VISIBLE

        }
    }

    private fun getPieMate() {

    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.ivBack->{
                super.onBackPressed()
            }
        }

    }
}
