package com.pie.ui.base

import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.adawatie.data.network.model.CommonResponse
import com.adawatie.utils.ResponseCode
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.pie.PieApp
import com.pie.R
import com.pie.data.network.RequestInterface
import com.pie.model.BaseResponse
import com.pie.ui.login.LoginActivity
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import com.pie.utils.MyAppProgressDialog
import com.pie.utils.PermissionUtils
import com.google.gson.Gson
import com.irozon.sneaker.Sneaker
import com.makeramen.roundedimageview.RoundedImageView
import com.pie.model.FollowResponse
import com.wang.avi.AVLoadingIndicatorView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.json.JSONObject
import retrofit2.HttpException
import java.util.*
import kotlin.collections.set

open class BaseActivity : AppCompatActivity() {
    val requestInterface by lazy {
        RequestInterface.create()
    }
    val pref by lazy {
        PieApp.getInstance().getAppPreferencesHelper()
    }
    var permissionUtils: PermissionUtils? = null
    val loaderDialog: MyAppProgressDialog by lazy {
        MyAppProgressDialog(this)
    }


    protected val gson by lazy { Gson() }
    // protected val encrypt by lazy { Encrypt() }
    //  protected val shake by lazy { AnimationUtils.loadAnimation(this, R.anim.shake) }

    protected lateinit var disposable: Disposable
    protected val TAG: String = this::class.java.canonicalName ?: this::class.java.name
    var handler: Handler = Handler()
    private var isConnected: Boolean = true
    var fab_open: Animation? = null
    var fab_close: Animation? = null
    protected var mCompositeDisposable = CompositeDisposable()
    private var noOfApiCall = 0
    private var onFailure: OnFailure? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close)
    }

    override fun onDestroy() {
        if (::disposable.isInitialized)
            disposable.dispose()
        super.onDestroy()
    }

    fun errorDialog(activity: Activity, message: String, vararg title: String) {
        AppGlobal.alertDialog(activity, message, *title)
    }

    fun getHeaderMap(): HashMap<String, Any> {
        val headerMap = HashMap<String, Any>()
        headerMap["Accept"] = "application/json"
        headerMap["Authorization"] = "Bearer " + pref.getToken()
        return headerMap
    }

    private fun showLoader() {
        loaderDialog.run()
    }

    private fun hideLoader() {
        loaderDialog.dismiss()
    }

    protected fun <T> callApi(
        observable: Observable<T>,
        vararg doSawLoader: Boolean
    ): Observable<T>? {
        if (!AppGlobal.isNetworkConnected(this)) {
            if (mCompositeDisposable.size() == 0)
                AppGlobal.networkAlertDialog(this)
            return Observable.create<T> {
                it.tryOnError(Exception(resources.getString(R.string.msg_no_internet)))
            }
        }
        if (doSawLoader.isNotEmpty() && doSawLoader[0]) {
            noOfApiCall++
            if (noOfApiCall == 1) {
                showLoader()
            }
        }
        return observable.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    private fun onResponse() {
        noOfApiCall--
        if (noOfApiCall <= 0) {
            noOfApiCall = 0
            hideLoader()
        }
    }

    protected fun onResponseFailure(throwable: Throwable, vararg doHideLoader: Boolean) {
        if (doHideLoader.isNotEmpty() && doHideLoader[0])
            onResponse()
     //   sneakerError(this,throwable.message ?: "")
        AppLogger.e("onResponseFailure", throwable.message ?: "")
        if (throwable is HttpException) {
            AppLogger.e("response code", "${throwable.code()}")
            errorHandling(throwable)
        }
    }

    private fun errorHandling(throwable: HttpException) {
        val errorRawData = throwable.response().errorBody()?.string()?.trim()
        if (errorRawData.isNullOrEmpty()) return

        when (throwable.code()) {
            ResponseCode.BadRequest.code -> {
                errorDialog(this, JSONObject(errorRawData).optString("message", ""))
            }
            ResponseCode.InValidateData.code -> {
                val jsonObject = JSONObject(errorRawData)
                val jObject = jsonObject.optJSONObject("errors")
                if (jObject != null) {
                    val keys: Iterator<String> = jObject.keys()
                    if (keys.hasNext()) {
                        while (keys.hasNext()) {
                            val msg = StringBuilder()
                            val key: String = keys.next()
                            if (jObject.get(key) is String) {
                                msg.append("- ${jObject.get(key)}\n")
                            }
                            errorDialog(this, msg.toString(), "Alert")
                        }
                    } else {
                        errorDialog(this, jsonObject.optString("message", ""))
                    }
                }
            }
            ResponseCode.Unauthenticated.code -> {
                onAuthFail()
                onFailure?.onFailure(throwable.code())
            }
            ResponseCode.OK.code -> {
            }
            ResponseCode.ServerError.code -> AppLogger.e(TAG, ResponseCode.ServerError.toString())
            ResponseCode.Unauthorized.code -> {
                errorDialog(this, JSONObject(errorRawData).optString("message", ""))
            }
            ResponseCode.NotFound.code -> {
                try {
                    errorDialog(this, JSONObject(errorRawData).optString("message", ""))
                }catch (e:java.lang.Exception){
                    e.printStackTrace()
                }

            }
            ResponseCode.Conflict.code -> {
                errorDialog(this, JSONObject(errorRawData).optString("message", ""))
            }
            ResponseCode.Blocked.code -> {
                errorDialog(this, JSONObject(errorRawData).optString("message", ""))
            }
            ResponseCode.ForceUpdate.code -> {
                errorDialog(this, JSONObject(errorRawData).optString("message", ""))
            }
        }
    }

    protected fun <T> onStatusFalse(t: BaseResponse<T>, vararg doSawLoader: Boolean): Boolean {
        if (doSawLoader.isNotEmpty() && doSawLoader[0]) {
            noOfApiCall--
            if (noOfApiCall <= 0) {
                noOfApiCall = 0
                hideLoader()
            }
        }
        if (t.success==0) {
            AppGlobal.alertDialog(this, t.message)
            return (t.success==0)
        }

        return false
    }

    protected fun  onStatusFalseFollow(t: FollowResponse, vararg doSawLoader: Boolean): Boolean {
        if (doSawLoader.isNotEmpty() && doSawLoader[0]) {
            noOfApiCall--
            if (noOfApiCall <= 0) {
                noOfApiCall = 0
                hideLoader()
            }
        }
        if (t.success==0) {
            AppGlobal.alertDialog(this, t.message)
            return (t.success==0)
        }

        return false
    }

    @Suppress("unused")
    protected fun <T> onStatusFalseNoMsg(t: T, vararg doSawLoader: Boolean): Boolean {
        if (doSawLoader.isNotEmpty() && doSawLoader[0]) {
            noOfApiCall--
            if (noOfApiCall <= 0) {
                noOfApiCall = 0
                hideLoader()
            }
        }
        return t is CommonResponse && !(t as CommonResponse).success
    }

    fun setLayoutParams(dialog: Dialog) {
        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(dialog.window!!.attributes)
        lWindowParams.width =
            WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = lWindowParams
    }

    @Suppress("unused")
    fun setOnFail(onFailure: OnFailure) {
        this.onFailure = onFailure
    }

    interface OnFailure {
        fun onFailure(responseCode: Int)
    }

    private fun onAuthFail() {
        mCompositeDisposable.clear()
        pref.setLogin(false)
        startActivity(intentFor<LoginActivity>().clearTask().newTask())
    }

    fun setImage(imageView:RoundedImageView,image:String){
        Glide.with(this).load(image).into(imageView)
    }

    fun logoutDialog(message: String, avi: AVLoadingIndicatorView?) {
//        setLoader(avi)
//        val dialog = Dialog(this)
//        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
//        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setContentView(R.layout.dialog_alert)
//        dialog.setCanceledOnTouchOutside(true)
//        dialog.tvMessage.text = message
//        dialog.tvCancel.setOnClickListener { dialog.dismiss() }
//
//        dialog.tvOk.setOnClickListener {
//            dialog.dismiss()
//            logoutApi()
//        }
//
//        dialog.show()
    }

    fun startAnimation(shimmerFrameLayout: ShimmerFrameLayout?) {
        shimmerFrameLayout?.visibility = View.VISIBLE
        shimmerFrameLayout?.startShimmer()

    }

    fun stopAnimation(shimmerFrameLayout: ShimmerFrameLayout?) {
        shimmerFrameLayout?.visibility = View.GONE
        shimmerFrameLayout?.stopShimmer()
    }

    private fun clearNotifications() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    protected fun enableDisableView(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (idx in 0 until view.childCount) {
                enableDisableView(view.getChildAt(idx), enabled)
            }
        }
    }

    fun setLightStatusBar(view: View, activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
            activity.window.statusBarColor = Color.WHITE
        }
    }

    fun sneakerError(activity: Activity, message: String) {
        Sneaker.with(activity)
            .setIcon(R.drawable.ic_error, R.color.colorWhite, false)
            .setMessage(message, R.color.colorWhite)
            .sneak(android.R.color.holo_red_light)
    }

    fun sneakerSuccess(activity: Activity, message: String) {
        Sneaker.with(activity)
            .setIcon(R.drawable.ic_success, R.color.colorWhite, false)
            .setMessage(message, R.color.colorWhite)
            .sneak(R.color.colorGreen)
    }

   /*  fun logout() {
        val data = HashMap<String, Any>()
        callApi(requestInterface.logout(data), false)
            ?.subscribe({ onLogout(it) }) { onResponseFailure(it, true) }
            ?.let { mCompositeDisposable.add(it) }
         onAuthFail()

    }


    private fun onLogout(resp: BaseResponse<Any>) {
        if (onStatusFalse(resp, true)) return
        AppLogger.e("json++ =>", gson.toJson(resp.data))
        if (resp.success) {
          onAuthFail()
        }
    }*/

}