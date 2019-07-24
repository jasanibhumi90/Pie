package com.pie.ui.register

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.LoginModel
import com.pie.ui.base.BaseActivity
import com.pie.ui.verification.VerificationActivity
import com.pie.utils.AppConstant.Companion.ARG_DATA
import com.pie.utils.AppConstant.Companion.ARG_PIN_CODE
import com.pie.utils.AppGlobal
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*


class RegisterActivity : BaseActivity(), View.OnClickListener {

    var vFirstName = ""
    var vLastName = ""
    var vEmailId = ""
    var vPhoneNo = ""
    var vPassword = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        tvRegister.setOnClickListener(this)
        ivBack.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvRegister -> {
                if (isValid()) {
                    registerApi()
                }
            }
            R.id.ivBack -> {
                finish()
            }


        }
    }

    private fun isValid(): Boolean {
        vFirstName = etFirstName.text.toString().trim()
        vLastName = etLastName.text.toString().trim()
        vEmailId = etEmailAddress.text.toString().trim()
        vPhoneNo = etMobileNo.text.toString().trim()
        vPassword = etPassword.text.toString().trim()

        if (vFirstName.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_first_name))
            return false
        } else if (vLastName.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_last_name))
            return false
        } else if (vEmailId.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_email))
            return false
        } else if (!AppGlobal.isEmailValid(vEmailId)) {
            sneakerError(this, resources.getString(R.string.error_valid_email))
            return false
        } else if (vPhoneNo.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_phone_no))
            return false
        } else if (vPassword.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_password))
            return false
        }
        return true
    }


    private fun registerApi() {
        if (AppGlobal.isNetworkConnected(this)) run {

            val data = HashMap<String, String>()
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            data[getString(R.string.param_first_name)] = vFirstName
            data[getString(R.string.param_last_name)] = vLastName
            data[getString(R.string.param_email)] = vEmailId
            data[getString(R.string.param_password)] = vPassword
            data[getString(R.string.param_country_name)] = "India"
            data[getString(R.string.param_country_code)] = "91"
            data[getString(R.string.param_phone_no)] = vPhoneNo
            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_signup)
            service[getString(R.string.request)] = request
            callApi(requestInterface.register(service), true)
                ?.subscribe({ onRegister(it, data) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onRegister(
        resp: BaseResponse<LoginModel>,
        data: HashMap<String, String>
    ) {
        if (super.onStatusFalse(resp, true)) return
            toast(resp.otpcode.toString())
            startActivity<VerificationActivity>(ARG_DATA to data,ARG_PIN_CODE to resp.otpcode.toString() )

    }

}
