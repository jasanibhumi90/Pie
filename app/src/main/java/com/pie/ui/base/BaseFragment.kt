package com.pie.ui.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.adawatie.data.network.model.CommonResponse
import com.adawatie.utils.ResponseCode.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.pie.PieApp
import com.pie.R
import com.pie.data.network.RequestInterface
import com.pie.ui.login.LoginActivity
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import com.pie.utils.MyAppProgressDialog
import com.google.gson.Gson
import com.irozon.sneaker.Sneaker
import com.pie.utils.PermissionUtils
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


@Suppress("unused")
abstract class BaseFragment : Fragment() {
    val pref by lazy {
        PieApp.getInstance().getAppPreferencesHelper()
    }
    val requestInterface by lazy {
        RequestInterface.create()
    }
    val permissionUtils: PermissionUtils by lazy {
        PermissionUtils(this)
    }
    protected val gson by lazy { Gson() }
    //protected val encrypt by lazy { Encrypt() }
    private var loaderDialog: MyAppProgressDialog? = null

    @Suppress("PropertyName")
    protected val TAG: String = this::class.java.canonicalName ?: this::class.java.name

    protected var shake: Animation? = null
    var fab_open: Animation? = null
    var fab_close: Animation? = null
    protected lateinit var mActivity: BaseActivity
    protected var handler: Handler = Handler()
    private var mContext: Context = PieApp.getInstance()
    protected var mCompositeDisposable = CompositeDisposable()
    private var noOfApiCall = 0
    private var onFailure: OnFailure? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
        loaderDialog = activity?.let { MyAppProgressDialog(it) }
        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(mContext, R.anim.fab_close)
    }

    private var disposable: Disposable? = null

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity) {
            val activity = context as BaseActivity?
            this.mActivity = activity!!
        }

    }

    override fun onDetach() {
        super.onDetach()
        if (context is BaseActivity) {
            val activity = context as BaseActivity?
            this.mActivity = activity!!
        }
    }

    fun getHeaderMap(): HashMap<String, Any> {
        val headerMap = HashMap<String, Any>()
        headerMap["Accept"] = "application/json"
        headerMap["Authorization"] = "Bearer " + pref.getToken()
        return headerMap
    }

    fun errorDialog(activity: Activity?, message: String, vararg title: String) {
        AppGlobal.alertDialog(activity, message, *title)
    }

    private fun showLoader() {
        loaderDialog?.run()
    }

    private fun hideLoader() {
        loaderDialog?.dismiss()
    }

    protected fun <T> callApi(
        observable: Observable<T>,
        vararg doSawLoader: Boolean
    ): Observable<T>? {
        if (!AppGlobal.isNetworkConnected(mContext)) {
            if (mCompositeDisposable.size() == 0)
                AppGlobal.networkAlertDialog(context)
            return Observable.create<T> {
                it.tryOnError(Exception(mContext.resources.getString(R.string.msg_no_internet)))
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
        AppLogger.e("onResponseFailure", throwable.message ?: "", throwable)
        if (throwable is HttpException) {
            AppLogger.e("response code", "${throwable.code()}")
            errorHandling(throwable)
        }
    }

    private fun errorHandling(throwable: HttpException) {
        val errorRawData = throwable.response().errorBody()?.string()?.trim()
        if (errorRawData.isNullOrEmpty()) return

        when (throwable.code()) {
            BadRequest.code -> {
                errorDialog(activity, JSONObject(errorRawData).optString("message", ""))
            }
            InValidateData.code -> {
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
                            errorDialog(activity, msg.toString(), "Alert")
                        }
                    } else {
                        errorDialog(activity, jsonObject.optString("message", ""))
                    }
                }
            }
            Unauthenticated.code -> {
                onAuthFail()
                onFailure?.onFailure(throwable.code())
            }
            OK.code -> {
            }
            ServerError.code -> AppLogger.e(TAG, ServerError.toString())
            Unauthorized.code -> {
                errorDialog(activity, JSONObject(errorRawData).optString("message", ""))
            }
            NotFound.code -> {
                errorDialog(activity, JSONObject(errorRawData).optString("message", ""))
            }
            Conflict.code -> {
                errorDialog(activity, JSONObject(errorRawData).optString("message", ""))
            }
            Blocked.code -> {
                errorDialog(activity, JSONObject(errorRawData).optString("message", ""))
            }
            ForceUpdate.code -> {
                errorDialog(activity, JSONObject(errorRawData).optString("message", ""))
            }
        }
    }

    protected fun <T> onStatusFalse(t: T, vararg doSawLoader: Boolean): Boolean {
        if (doSawLoader.isNotEmpty() && doSawLoader[0]) {
            noOfApiCall--
            if (noOfApiCall <= 0) {
                noOfApiCall = 0
                hideLoader()
            }
        }
        if (t is CommonResponse) {
            if (!(t as CommonResponse).success) {
                AppGlobal.alertDialog(mActivity, t.message ?: "")
            }
            return !(t as CommonResponse).success
        }
        return false
    }

    protected fun <T> onStatusFalseNoMessage(t: T, vararg doSawLoader: Boolean): Boolean {
        if (doSawLoader.isNotEmpty() && doSawLoader[0]) {
            noOfApiCall--
            if (noOfApiCall <= 0) {
                noOfApiCall = 0
                hideLoader()
            }
        }
        if (t is CommonResponse) {
            return !(t as CommonResponse).success
        }
        return false
    }

    fun setOnFail(onFailure: OnFailure) {
        this.onFailure = onFailure
    }

    fun startAnimation(shimmerFrameLayout: ShimmerFrameLayout?) {
        shimmerFrameLayout?.visibility = View.VISIBLE
        shimmerFrameLayout?.startShimmer()

    }

    fun stopAnimation(shimmerFrameLayout: ShimmerFrameLayout?) {
        shimmerFrameLayout?.visibility = View.GONE
        shimmerFrameLayout?.stopShimmer()
    }


    interface OnFailure {
        fun onFailure(responseCode: Int)
    }

    private fun onAuthFail() {
        mCompositeDisposable.clear()
        pref.setLogin(false)
        mActivity.startActivity(mActivity.intentFor<LoginActivity>().clearTask().newTask())
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
    }

    fun setLayoutParams(dialog: Dialog) {
        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(dialog.window!!.attributes)
        lWindowParams.width =
            WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = lWindowParams
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


}