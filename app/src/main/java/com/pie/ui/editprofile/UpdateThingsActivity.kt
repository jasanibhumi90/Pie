package com.pie.ui.editprofile

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.ThingsModel
import com.pie.ui.base.BaseActivity
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.activity_edit_update_things.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.apmem.tools.layouts.FlowLayout
import java.util.*


class UpdateThingsActivity : BaseActivity(), View.OnClickListener {
    val arThings: ArrayList<ThingsModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_update_things)

        ivBack.setOnClickListener(this)
        tvTitle.text = resources.getString(R.string.select_or_change_your_interest)
        getThings()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
              onBackPressed()
            }


        }
    }

    private fun isValid(): Boolean {

        return true
    }


    private fun getThings() {
        if (AppGlobal.isNetworkConnected(this)) run {
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            service[getString(R.string.service)] = getString(R.string.service_get_things)
            service[getString(R.string.request)] = request

            callApi(requestInterface.getThings(service), true)
                ?.subscribe({ onGetReports(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun onGetReports(
        resp: BaseResponse<ArrayList<ThingsModel>>
    ) {
        if (super.onStatusFalse(resp, true)) return

        if (resp.data?.size != 0) {

            resp.data?.let { arThings.addAll(it) }

            for (i in 0 until pref.getLoginData()?.things_ids?.split(",")?.size!!) {
                val data = pref.getLoginData()?.things_ids?.split(",")!![i]
                arThings.map {
                    if (it.id == data) {
                        it.isSelected = true
                    }
                }
            }
            addView(flowLayout)
        }
    }



    override fun onBackPressed() {
        val data=Intent()
        val arSelectedData = arThings.filter { it.isSelected }

        data.putExtra("things_ids",    TextUtils.join(",", arSelectedData.map { it.id }))
        setResult(Activity.RESULT_OK,data)
        finish()
    }

    fun addView(flowLayout: FlowLayout) {
        if (flowLayout.childCount != 0) {
            flowLayout.removeAllViews()
        }
        var prevPos = 0
        var pos = 0

        for (i in 0 until arThings.size) {
            val data = arThings[i]
            val v = LayoutInflater.from(flowLayout.context)
                .inflate(R.layout.flow_things, flowLayout, false) as TextView

            val newDrawable: Drawable =
                DrawableCompat.wrap(resources.getDrawable(R.drawable.bg_btn_radius_twenty))
            DrawableCompat.setTint(newDrawable, resources.getColor(R.color.colorInvitesBg))
            //  v.background = newDrawable

            v.text = data.things_name
            v.setOnClickListener {
                pos = i
                if (arThings.get(pos).isSelected) {
                    arThings.get(pos).isSelected = false
                    flowLayout.getChildAt(pos).background =
                        resources.getDrawable(R.drawable.bg_editext)
                    flowLayout.getChildAt(pos)
                    flowLayout.getChildAt(pos).findViewById<TextView>(R.id.tvThingsName)
                        .setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                } else {
                    arThings.get(pos).isSelected = true
                   selectView(pos)
                }
            }
            flowLayout.addView(v, flowLayout.childCount)
            if(data.isSelected) {
                selectView(i)
            }
        }
    }


    fun selectView(pos:Int){
        flowLayout.getChildAt(pos).background =
            resources.getDrawable(R.drawable.bg_btn_radius_thirty)
        flowLayout.getChildAt(pos).findViewById<TextView>(R.id.tvThingsName)
            .setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
    }


}
