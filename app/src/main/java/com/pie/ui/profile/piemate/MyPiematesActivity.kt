package com.pie.ui.profile.piemate

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.utils.RxBus
import com.pie.R
import com.pie.model.FollowResponse
import com.pie.model.Piemate
import com.pie.ui.base.BaseActivity
import com.pie.utils.AppConstant.Companion.ARG_FOLLOW_STATUS
import com.pie.utils.AppConstant.Companion.ARG_PIEMATE
import com.pie.utils.AppConstant.Companion.ARG_PIE_MATE_LIST
import com.pie.utils.AppConstant.Companion.ARG_POSITION
import kotlinx.android.synthetic.main.activity_my_piemates.*
import kotlinx.android.synthetic.main.toolbar_common.*

class MyPiematesActivity : BaseActivity(),View.OnClickListener {
    private lateinit var pieMatesAdapter:PiematesAdapter
    private var dialog:Dialog?=null

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

    private fun followUser(pos: Int) {
        val request = java.util.HashMap<String, Any>()
        val service = java.util.HashMap<String, Any>()
        val data = java.util.HashMap<String, Any>()
        val auth = java.util.HashMap<String, Any>()
        data[getString(R.string.param_follow_id)] = pieMatesAdapter.getItem(pos).user_id
        auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
        auth[getString(R.string.param_token)] = pref.getToken()
        request[getString(R.string.data)] = data
        service[getString(R.string.service)] = getString(R.string.service_pies_follow)
        service[getString(R.string.request)] = request
        service[getString(R.string.auth)] = auth
        callApi(requestInterface.followPie(service), true)
            ?.subscribe({
                onFollowUser(it,pos)
            }) {
                onResponseFailure(it, true)
            }
            ?.let { mCompositeDisposable.add(it) }

    }

    private fun onFollowUser(resp: FollowResponse, pos: Int) {
        if (super.onStatusFalseFollow(resp, true)) return
        val data = pieMatesAdapter.getItem(pos)
        data.followstatus=resp.followstatus
        pieMatesAdapter.updateItem(pos,data)
        if(pieMatesAdapter.getItem(pos).user_id==pref.getLoginData()?.user_id) {
            val bundle = Bundle()
            bundle.putString(ARG_FOLLOW_STATUS, resp.followstatus)
            bundle.putInt(ARG_POSITION, pos)
            bundle.putString(
                ARG_PIEMATE,
                pieMatesAdapter.getAll().map { it.followstatus == "2" }.size.toString()
            )
            RxBus.publish(Bundle(bundle))
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.ivBack->{
                super.onBackPressed()
            }
            R.id.tvFollow->{
                val pos=p0.tag as Int
                followUser(pos)
            }
        }

    }
}
