package com.pie.ui.verification

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.LoginModel
import com.pie.ui.base.BaseActivity
import com.pie.ui.main.MainActivity
import com.pie.ui.register_profile.RegisterProfileActivity
import com.pie.utils.AppConstant.Companion.ARG_DATA
import com.pie.utils.AppConstant.Companion.ARG_PIN_CODE
import com.pie.utils.AppGlobal
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_verification.*
import kotlinx.android.synthetic.main.activity_verification.ivBack
import org.jetbrains.anko.startActivity


class VerificationActivity : BaseActivity(), View.OnClickListener {

    var vCode = ""
    var vOtpCode = ""
    var data:HashMap<String,String> = HashMap()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        tvContinue.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        tvVerificationDesc.text = HtmlCompat.fromHtml(
            resources.getString(R.string.verification_has_been_sent, "9904250336"),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        if(intent.hasExtra(ARG_DATA)){
            data=intent.getSerializableExtra(ARG_DATA) as HashMap<String, String>
        }

        if(intent.hasExtra(ARG_PIN_CODE)){
            vOtpCode=intent.getStringExtra(ARG_PIN_CODE)
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvContinue -> {
                if(isValid()) {
                   registerApi()
                }
            }
            R.id.ivBack -> {
                finish()
            }


        }
    }

    private fun isValid(): Boolean {
        vCode = etCode.text.toString().trim()

        if (vCode.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_code))
            return false
        } else if (vCode!=vOtpCode) {
            sneakerError(this, resources.getString(R.string.error_valid_code))
            return false
        }
        return true
    }
    private fun registerApi() {
        if (AppGlobal.isNetworkConnected(this)) run {


            val request = java.util.HashMap<String, Any>()
            val service = java.util.HashMap<String, Any>()
            data[getString(R.string.param_mobile_code)] = vCode
            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_signup)
            service[getString(R.string.request)] = request
            callApi(requestInterface.register(service), true)
                ?.subscribe({ onRegister(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onRegister(
        resp: BaseResponse<LoginModel>
    ) {
        if (super.onStatusFalse(resp, true)) return
        startActivity<RegisterProfileActivity>(ARG_DATA to data)
    }

}
