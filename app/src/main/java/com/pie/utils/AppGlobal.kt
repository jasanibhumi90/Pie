package com.pie.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import com.pie.R
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

@Suppress("NAME_SHADOWING")

class AppGlobal {

    companion object {

        val DATEFORMAT = "yyyy-MM-dd HH:mm:ss"
        val TIMEZONE = "AST"
        val tag: String = this::class.java.canonicalName!!
        private fun containsURL(content:String):Boolean {
            val REGEX = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
            val p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE)
            val m = p.matcher(content)
            if (m.find())
            {
                return true
            }
            return false
        }
        fun isEmailValid(email: String): Boolean {
            val pattern: Pattern
            val matcher: Matcher
            val EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
            pattern = Pattern.compile(EMAIL_PATTERN)
            matcher = pattern.matcher(email)
            return matcher.matches()
        }

        @SuppressLint("MissingPermission")
        fun isNetworkConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }

        @SuppressLint("all")
        fun getDeviceId(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }

        fun getUTCDateTime(): String {
            val calendar = Calendar.getInstance()

            val df = SimpleDateFormat(DATEFORMAT, Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")

            return df.format(calendar.time)
        }

        fun hideSoftInput(activity: Activity) {
            var view = activity.currentFocus
            if (view == null) view = View(activity)
            val imm = activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun showSoftInput(edit: EditText, context: Context) {
            edit.isFocusable = true
            edit.isFocusableInTouchMode = true
            edit.requestFocus()
            val imm = context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(edit, 0)
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun pxToDp(px: Int): Int {
            return (px / Resources.getSystem().displayMetrics.density).toInt()
        }

        fun getDeviceHeight(context: Activity): Int {
            val displaymetrics = DisplayMetrics()
            context.windowManager.defaultDisplay.getMetrics(displaymetrics)
            return displaymetrics.heightPixels
        }

        fun getDeviceWidth(context: Activity): Int {
            val displaymetrics = DisplayMetrics()
            context.windowManager.defaultDisplay.getMetrics(displaymetrics)
            return displaymetrics.widthPixels
        }

        fun createPartFromString(message: String): RequestBody {
            return RequestBody.create(MediaType.parse("multipart/form-data"), message)
        }

        fun showToast(context: Context, message: String) {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }



        fun getSystemDateFromTimeZone(utcDateString: String, format: String): String {
            AppLogger.e(tag, "utcDateString: $utcDateString")
            var systemDateString = ""
            try {

                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone(TIMEZONE)
                val utcDate = formatter.parse(utcDateString)

                val dateFormatter = SimpleDateFormat(format, Locale.getDefault()) //this format changeable
                dateFormatter.timeZone = TimeZone.getDefault()
                systemDateString = dateFormatter.format(utcDate)

                //Log.d("ourDate", ourDate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            AppLogger.e(tag, "systemDateString: $systemDateString")
            return systemDateString
        }

        fun setDialogTheme(activity: Activity): Context? {
            return ContextThemeWrapper(activity, android.R.style.Theme_Light_NoTitleBar)
        }

        fun getAppDirectory(context: Context): File {
            val file = File(
                Environment.getExternalStorageDirectory().toString()
                        + "/" + context.getString(R.string.app_name)
            )

            if (!file.exists())
                file.mkdirs()

            return file
        }

        fun getRequestBody(value: String): RequestBody {
            return RequestBody.create(MediaType.parse("text/plain"), value)
        }

        fun shareCommonViaOther(activity: Activity, shareText: String) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            activity.startActivity(intent)
        }

        fun containsUrl(url: String): Boolean {
            val URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$"
            val p = Pattern.compile(URL_REGEX)
            val m = p.matcher(url)//replace with string to compare
            return m.find()
        }



        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                val runningProcesses = am.getRunningAppProcesses()
                for (processInfo in runningProcesses) {
                    if (processInfo.importance === ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.getPackageName()) {
                                isInBackground = false
                            }
                        }
                    }
                }
            } else {
                val taskInfo = am.getRunningTasks(1)
                val componentInfo = taskInfo.get(0).topActivity
                if (componentInfo.getPackageName().equals(context.getPackageName())) {
                    isInBackground = false
                }
            }
            return isInBackground
        }
        fun networkAlertDialog(context: Context?) {
            context?.alert(
                context.resources.getString(R.string.msg_no_internet),
                context.getString(R.string.app_name)
            ) { okButton { } }?.show()
        }

        fun alertDialog(context: Context?, message: String, vararg title: String) {
            if (context == null) return
            if (message.isEmpty()) return
            val displayTitle =
                if (title.isEmpty()) context.getString(R.string.app_name) ?: "Adawatie"
                else title[0]
            context.alert(message, displayTitle) { okButton { it.dismiss() } }.show()
//            try {
//                context?.let { context ->
//                    val dialog = Dialog(context)
//                    dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
//                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                    dialog.setContentView(R.layout.dialog_alert)
//                    dialog.setCanceledOnTouchOutside(true)
//                    dialog.tvMessage.text = message
//                    dialog.tvCancel.visibility = View.GONE
//                    dialog.tvCancel.setOnClickListener { dialog.dismiss() }
//
//                    dialog.tvOk.setOnClickListener {
//                        dialog.dismiss()
//                        if (type.isNotEmpty() && type[0] == N_TYPE_REFFERAL) {
//                            context.startActivity<MyRefferalsActivity>()
//                        } else if (type.isNotEmpty() && type[0] == ALERT_COMPLETE_PROFILE) {
//                            val profile = HaochiApp.getInstance().getAppPreferencesHelper().getUserProfile()
//                            context.startActivity<EditProfileActivity>(AppConstant.ARG_PROFILE_DATA to profile)
//                        }
//                    }
//                    loadNativeAds(dialog.flNativeAd)
//                    dialog.show()
//                }
//            } catch (e: Exception) {
//            }
        }




    }
}
