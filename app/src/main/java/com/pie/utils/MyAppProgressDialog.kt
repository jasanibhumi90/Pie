package com.pie.utils

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.pie.R
import kotlinx.android.synthetic.main.dialog_loader.view.*

class MyAppProgressDialog(val activity: Activity?) : Thread() {

    private var dialog: Dialog? = Dialog(AppGlobal.setDialogTheme(this.activity!!)!!)
    var view: View? = null

    init {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            dialog?.window?.statusBarColor = ContextCompat.getColor(this.activity!!, android.R.color.transparent)
            dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        view = activity?.layoutInflater?.inflate(R.layout.dialog_loader, null)
        view!!.tvProgress.visibility = View.GONE

        dialog?.setContentView(view!!)

        setCanceledOnTouchOutside(false)
        setCancelable(true)

    }

    override fun run() {
        super.run()
        try {
            activity?.runOnUiThread {
                if (!activity.isFinishing()) {
                    dialog?.show()

                }
            }
        } catch (e: WindowManager.BadTokenException) {
        }

    }

    fun dismiss() {
        dialog?.dismiss()
        this.interrupt()
    }

    fun setCancelable(flag: Boolean) {
        dialog?.setCancelable(flag)
    }

    fun setCanceledOnTouchOutside(flag: Boolean) {
        dialog?.setCanceledOnTouchOutside(flag)
    }

    fun showProgress(flag: Boolean) {
        if (flag) {
            view!!.tvProgress.visibility = View.VISIBLE
        } else {
            view!!.tvProgress.visibility = View.GONE
        }
    }

    fun setProgress(percent: Int) {
        view!!.tvProgress.text = "$percent%"
    }
}