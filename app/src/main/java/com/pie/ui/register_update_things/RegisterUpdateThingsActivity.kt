package com.pie.ui.register_update_things

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
import com.pie.ui.main.MainActivity
import com.pie.utils.AppGlobal
import kotlinx.android.synthetic.main.activity_update_things.*
import org.apmem.tools.layouts.FlowLayout
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import java.util.*


class RegisterUpdateThingsActivity : BaseActivity(), View.OnClickListener {
    val arThings: ArrayList<ThingsModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_things)

        ivBack.setOnClickListener(this)
        tvDone.setOnClickListener(this)
        getThings()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvDone -> {
                if (isValid())
                    updateThings()
            }
        }
    }

    private fun isValid(): Boolean {
        if (arThings.filter { it.isSelected }.size < 5) {
            sneakerError(this, getString(R.string.things_error))
            return false
        }
        return true
    }

    private fun updateThings() {
        val request = HashMap<String, Any>()
        val service = HashMap<String, Any>()
        val data = HashMap<String, Any>()
        val auth = HashMap<String, Any>()

        val arSelectedData = arThings.filter { it.isSelected }
        data[getString(R.string.param_Things_ids)] =
            TextUtils.join(",", arSelectedData.map { it.id })
        auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
        auth[getString(R.string.param_token)] = pref.getToken()
        request[getString(R.string.data)] = data
        service[getString(R.string.service)] = getString(R.string.service_update_things)
        service[getString(R.string.request)] = request
        service[getString(R.string.auth)] = auth
        callApi(requestInterface.updateThings(service), true)
            ?.subscribe({ onUpdateThingsResponse(it) }) { onResponseFailure(it, true) }
            ?.let { mCompositeDisposable.add(it) }

    }

    private fun onUpdateThingsResponse(resp: BaseResponse<Any>) {
        if (super.onStatusFalse(resp, true)) return
        startActivity(intentFor<MainActivity>().clearTask().newTask())
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

    private fun onGetReports(resp: BaseResponse<ArrayList<ThingsModel>>) {
        if (super.onStatusFalse(resp, true)) return
        if (resp.data?.size != 0) {
            resp.data?.let { arThings.addAll(it) }
            addView(flowLayout)
        }
    }

    private fun addView(flowLayout: FlowLayout) {
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
                    flowLayout.getChildAt(pos).background =
                        resources.getDrawable(R.drawable.bg_btn_white_thirty)
                    flowLayout.getChildAt(pos).findViewById<TextView>(R.id.tvThingsName)
                        .setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                }
            }
            flowLayout.addView(v, flowLayout.childCount)
        }
    }


}
