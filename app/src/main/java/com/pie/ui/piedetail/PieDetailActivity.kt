package com.pie.ui.piedetail

import android.app.Dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.utils.RxBus
import com.pie.PieApp
import com.pie.R
import com.pie.model.*
import com.pie.ui.base.BaseActivity
import com.pie.ui.editpie.EditPieActivity
import com.pie.ui.home.comment.CommentsAdapter2
import com.pie.ui.piedetail.like.LikeAdapter
import com.pie.utils.AppConstant
import com.pie.utils.AppConstant.Companion.ARG_DETAIL_COMMENT
import com.pie.utils.AppConstant.Companion.ARG_DETAIL_DELETE
import com.pie.utils.AppConstant.Companion.ARG_DETAIL_LIKE
import com.pie.utils.AppConstant.Companion.ARG_DETAIL_UPDATE
import com.pie.utils.AppConstant.Companion.ARG_ISFROM_PIEDETAIL
import com.pie.utils.AppConstant.Companion.ARG_ISLIKE
import com.pie.utils.AppConstant.Companion.ARG_ISREPLY
import com.pie.utils.AppConstant.Companion.ARG_PIE_DATA
import com.pie.utils.AppConstant.Companion.ARG_PIE_ID
import com.pie.utils.AppConstant.Companion.ARG_POSITION
import com.pie.utils.AppConstant.Companion.DATE_APP_TYPE
import com.pie.utils.AppConstant.Companion.TIME_APP_TYPE
import com.pie.utils.AppConstant.Companion.WEB_DATE_TYPE
import com.pie.utils.AppGlobal
import com.pie.utils.AppGlobal.Companion.formatDateTime
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.activity_pie_detail.*
import kotlinx.android.synthetic.main.dialog_alert.*
import kotlinx.android.synthetic.main.dialog_like.*
import kotlinx.android.synthetic.main.dialog_reportpost.*
import kotlinx.android.synthetic.main.flow_report.view.*
import kotlinx.android.synthetic.main.raw_comment_reply_list.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.apmem.tools.layouts.FlowLayout
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList

class PieDetailActivity : BaseActivity(), View.OnClickListener,
    CommentsAdapter2.OnItemViewClickListener {
    var commentsAdapter: CommentsAdapter2 =
        CommentsAdapter2(PieApp.getInstance(), ArrayList())
    private lateinit var likeAdapter: LikeAdapter
    private lateinit var likeDialog: Dialog
    var isCommentReply: Boolean = false
    private var replyId: String = "0"
    private var parentId: String = ""
    private var pieId: String = ""
    private lateinit var likeResp: ArrayList<LikeModel>
    private var likeList = ArrayList<LikeModel>()
    val arReasons: ArrayList<ReportModel> = ArrayList()
    private lateinit var reportPostdialog: Dialog
    private var reasonName = ""
    private var reasonId = 0
    private var position: Int = 0
    private var userId: String = ""
    private lateinit var pieData:PostModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_detail)
        if (intent.hasExtra(ARG_PIE_ID)) {
            position = intent.getIntExtra(AppConstant.ARG_POSITION, 0)
            init()
            clickListener()
            getPieDetail(intent.getIntExtra(ARG_PIE_ID, 0))
        }
    }

    private fun clickListener() {
        tvSend.setOnClickListener(this)
        tvLikes.setOnClickListener(this)
        llLikeLayout.setOnClickListener(this)
        ivMenu.setOnClickListener(this)
        ivReplyCloseDetail.setOnClickListener(this)
    }

    fun init() {
        rvComments.isNestedScrollingEnabled = false
        tvTitle.text = getString(R.string.pie)
        consPieDetail.visibility = View.GONE
        setCommentAdapter(ArrayList())
    }

    private fun setCommentAdapter(comments: List<CommentModel>) {
        rvComments.layoutManager = LinearLayoutManager(this)
        commentsAdapter = CommentsAdapter2(this, comments)
        commentsAdapter.setOnItemViewClickListener(this)
        rvComments.adapter = commentsAdapter
        commentsAdapter.collapseAllParents()
    }

    private fun getPieDetail(id: Int) {
        val request = HashMap<String, Any>()
        val service = HashMap<String, Any>()
        val data = HashMap<String, Any>()
        val auth = HashMap<String, Any>()
        data[getString(R.string.param_pie_id)] = id
        auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
        auth[getString(R.string.param_token)] = pref.getToken()
        request[getString(R.string.data)] = data
        service[getString(R.string.service)] = getString(R.string.service_get_pies_view)
        service[getString(R.string.request)] = request
        service[getString(R.string.auth)] = auth
        callApi(requestInterface.getPiePostComment(service), true)
            ?.subscribe({
                onGetPieResponse(it)
            }) {
                onResponseFailure(it, true)
            }
            ?.let { mCompositeDisposable.add(it) }
    }

    private fun onGetPieResponse(resp: BaseResponse<GetPieView>) {
        if (super.onStatusFalse(resp, true)) return
        resp.data?.let {

            pieData= PostModel(id=it.id,user_id = it.user_id,pie_user_id = it.pie_user_id,parent_id = it.parent_id,
            post_type = it.post_type,pies_text = it.pies_text,pies_type = it.post_type,pies_media = it.pies_media,
                profile_pic = it.profile_pic,first_name = it.first_name,last_name = it.last_name,creation_datetime = it.creation_datetime,
                likes = it.likes,comments = it.comments,view_count = it.view_count,shared = it.shared,pies_media_url=it.pies_media_url,
                post_at = it.post_at,like_flag = it.like_flag)
            if (it.profile_pic.isNotEmpty()) {
                setImage(ivProfile, it.profile_pic)
            }
            userId = it.user_id
            consPieDetail.visibility = View.VISIBLE
            tvName.text = it.first_name
            tvUserName.text = it.last_name
            tvDate.text = formatDateTime(it.creation_datetime, WEB_DATE_TYPE, DATE_APP_TYPE)
            tvTime.text = formatDateTime(it.creation_datetime, WEB_DATE_TYPE, TIME_APP_TYPE)
            tvPostDesc.text = it.pies_text
            llOne.visibility = View.GONE
            ivImage1.visibility = View.GONE
            ivImage2.visibility = View.GONE
            pieId = it.id
            parentId = it.parent_id
            llTwo.visibility = View.GONE
            ivImage3.visibility = View.GONE
            ivImage4.visibility = View.GONE
            if (!it.pies_media_url.isNullOrEmpty() && it.pies_media_url.size > 0) {
                if (it.pies_media_url.size == 1) {
                    llOne.visibility = View.VISIBLE
                    ivImage1.visibility = View.VISIBLE
                    setImage(ivImage1, it.pies_media_url[0])
                } else if (it.pies_media_url.size == 2) {
                    llOne.visibility = View.VISIBLE
                    ivImage1.visibility = View.VISIBLE
                    setImage(ivImage1, it.pies_media_url[0])
                    ivImage2.visibility = View.VISIBLE
                    setImage(ivImage2, it.pies_media_url[1])
                } else if (it.pies_media_url.size == 3) {
                    llOne.visibility = View.VISIBLE
                    llTwo.visibility = View.VISIBLE
                    ivImage1.visibility = View.VISIBLE
                    setImage(ivImage1, it.pies_media_url[0])
                    ivImage2.visibility = View.VISIBLE
                    setImage(ivImage2, it.pies_media_url[1])
                    ivImage3.visibility = View.VISIBLE
                    setImage(ivImage3, it.pies_media_url[2])
                } else if (it.pies_media_url.size == 4) {
                    llOne.visibility = View.VISIBLE
                    llTwo.visibility = View.VISIBLE

                    ivImage1.visibility = View.VISIBLE
                    setImage(ivImage1, it.pies_media_url[0])
                    ivImage2.visibility = View.VISIBLE
                    setImage(ivImage2, it.pies_media_url[1])
                    ivImage3.visibility = View.VISIBLE
                    setImage(ivImage3, it.pies_media_url[2])
                    ivImage4.visibility = View.VISIBLE
                    setImage(ivImage4, it.pies_media_url[3])
                }
            }
            tvTotalLike.text = it.likes
            tvLikes.tag = it.like_flag
            tvComments.text = it.comments
            tvViews.text = it.view_count
            setLikeReflect(it.like_flag.toString())
            if (it.comment_list.size > 0) {
                rvComments.visibility = View.VISIBLE
                llNodataFound.visibility = View.GONE
                commentsAdapter.appendAll(it.comment_list)
                AppLogger.e("tag", commentsAdapter.itemCount.toString())
            } else {
                rvComments.visibility = View.GONE
                llNodataFound.visibility = View.VISIBLE
            }
            commentsAdapter.collapseAllParents()
            AppLogger.e("tag", it.like_list.size.toString())
            if (it.like_list.isNotEmpty()) {
                likeResp = it.like_list
            }
        }
    }

    fun likeDialog() {
        try {
            likeDialog = Dialog(this)
            likeDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            likeDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            likeDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            likeDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            likeDialog.setContentView(R.layout.dialog_like)

            likeDialog.setCanceledOnTouchOutside(false)
            likeDialog.window.attributes.windowAnimations = R.style.DialogStyle
            startAnimation(likeDialog.shimmerViewContainerLike)
            likeDialog.ivDilogCloseLike.setOnClickListener(this)

            if (likeList.size > 0)
                likeList.clear()

            if (::likeResp.isInitialized && likeResp.size > 0) {
                likeDialog.rvLike.visibility = View.VISIBLE
                likeList.addAll(likeResp)
                setLikeAdapter(likeList)
                likeDialog.tvLikeTitle.text =
                    getString(R.string.total_like, likeResp.size.toString())
            } else {
                likeDialog.llNodataFoundLike.visibility = View.VISIBLE
            }

            val lp = WindowManager.LayoutParams()
            val window = likeDialog.window
            lp.copyFrom(window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = lp
            // commentsDialog.getWindow().setCallback(windowCallback);
            likeDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setLikeAdapter(likeResp: ArrayList<LikeModel>) {
        stopAnimation(likeDialog.shimmerViewContainerLike)
        likeDialog.rvLike.layoutManager = LinearLayoutManager(this)
        likeAdapter = LikeAdapter(R.layout.like_row_layout, this)
        likeDialog.rvLike.adapter = likeAdapter
        likeAdapter.addAll(likeResp)
        //AppLogger.e("tag","list.."+likeAdapter.itemCount+"..."+likeResp.size)
    }

    private fun setLikeReflect(islikeFlag: String) {
        if (islikeFlag == "0") {
            tvLikes.text = getString(R.string.likepost)
            tvLikes.setTextColor(resources.getColor(R.color.colorGrey500))
            tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.p_heart, 0, 0, 0)
        } else {
            tvLikes.setTextColor(resources.getColor(R.color.colorRed))
            tvLikes.text = getString(R.string.liked)
            tvLikes.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.p_heart_liked,
                0,
                0,
                0
            )
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvSend -> {
                isCommentReply = llReplyLbl.visibility == View.VISIBLE
                if (isCommentReply) {
                    val position = p0.tag as Int
                    postComment(
                        replyId,
                        pieId,
                        position,
                        edReplyComment.text.toString().trim(),
                        isCommentReply
                    )
                } else {
                    val position = 0
                    postComment(
                        parentId,
                        pieId,
                        position,
                        edReplyComment.text.toString().trim(),
                        isCommentReply
                    )
                }
            }
            R.id.tvLikes -> {
                if (pref.isLogin()) {
                    if (tvLikes.tag.toString() == "0")
                        tvLikes.tag = "1"
                    else
                        tvLikes.tag = "0"
                    setLikeReflect(tvLikes.tag.toString())
                    postLike()
                } else {
                    AppGlobal.alertDialog(this, getString(R.string.loginfirst_error))
                }
            }
            R.id.llLikeLayout -> {
                likeDialog()
            }
            R.id.ivDilogCloseLike -> {
                likeDialog.dismiss()
            }
            R.id.ivMenu -> {
                openMenu(p0, userId)
            }
            R.id.ivReportDilogClose -> {
                reportPostdialog.dismiss()
            }
            R.id.tvReportPostSend -> {
                if (isValid()) {
                    reportPost(
                        pieId,
                        reasonId,
                        reportPostdialog.edDescription.text.toString()
                    )
                }
            }
          /*  R.id.view_reply -> {
                view_reply.visibility = View.GONE
                llReplyLayout.visibility = View.VISIBLE
            }*/
            R.id.ivReplyCloseDetail->{
                llReplyLbl.visibility=View.GONE
            }

        }
    }

    private fun isValid(): Boolean {
        if (reasonName.isEmpty()) {
            AppGlobal.alertDialog(this, resources.getString(R.string.error_reason))
            return false
        } else if (reasonName.toLowerCase() == "other".toLowerCase() && reportPostdialog.edDescription.text.toString().isEmpty()) {
            AppGlobal.alertDialog(this, resources.getString(R.string.error_desc))
            return false
        }
        return true
    }

    private fun reportPost(postId: String, reason: Int, comment: String) {
        val request = HashMap<String, Any>()
        val service = HashMap<String, Any>()
        val data = HashMap<String, Any>()
        val auth = HashMap<String, Any>()
        data[getString(R.string.param_pie_id)] = postId
        data[getString(R.string.param_why_reporting)] = reason
        data[getString(R.string.param_reporting_text)] = comment

        auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
        auth[getString(R.string.param_token)] = pref.getToken()

        request[getString(R.string.data)] = data
        service[getString(R.string.service)] = getString(R.string.service_post_pies_report)
        service[getString(R.string.request)] = request
        service[getString(R.string.auth)] = auth
        callApi(requestInterface.postReport(service), true)
            ?.subscribe({ onGetPostReport(it) }) { onResponseFailure(it, true) }
            ?.let { mCompositeDisposable.add(it) }

    }

    private fun onGetPostReport(
        resp: BaseResponse<Any>
    ) {
        if (super.onStatusFalse(resp, true)) return
        reportPostdialog.dismiss()
        sneakerSuccess(this, resp.message)
    }

    private fun openMenu(v: View, userId: String) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.option_menu)
        if (pref.getLoginData()?.user_id == userId) {
            popup.menu.findItem(R.id.menu1).isVisible = true
            popup.menu.findItem(R.id.sharePost).isVisible = false
            popup.menu.findItem(R.id.menu1).title = resources.getString(R.string.menu_editpost)
            popup.menu.findItem(R.id.menu2).title = resources.getString(R.string.menu_deletepost)
        } else {
            popup.menu.findItem(R.id.menu1).isVisible = false
            popup.menu.findItem(R.id.sharePost).isVisible = true
            popup.menu.findItem(R.id.menu2).title = resources.getString(R.string.menu_reportpost)
        }
        popup.setOnMenuItemClickListener { item ->

            when (item.itemId) {
                R.id.menu1 -> {
                    if(::pieData.isInitialized) {
                        val intent = Intent(this, EditPieActivity::class.java)
                        intent.putExtra(ARG_PIE_DATA, pieData)
                        intent.putExtra(ARG_ISFROM_PIEDETAIL,true)
                        startActivity(intent)
                    }else{
                        toast(getString(R.string.something_wrong))
                    }
                }
                R.id.menu2 -> {
                    if (pref.getLoginData()?.user_id == userId) {
                        deletePie()
                    } else {
                        getReports()
                    }
                }
                R.id.sharePost -> {
                    shareAlertDialog()
                }

            }
            false
        }
        popup.show()
    }

    private fun shareAlertDialog() {
        val dialog = Dialog(this)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_alert)
        dialog.setCanceledOnTouchOutside(true)
        dialog.tvMessage.text = resources.getString(R.string.warning_share)
        dialog.tvOk.text = resources.getString(R.string.yes)
        dialog.tvCancel.text = resources.getString(R.string.no)
        dialog.tvCancel.setOnClickListener { dialog.dismiss() }

        dialog.tvOk.setOnClickListener {
            dialog.dismiss()
            sharePostApi()
        }
        dialog.show()
    }

    private fun getReports() {
        if (AppGlobal.isNetworkConnected(this)) run {
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_get_report)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.getReportss(service), true)
                ?.subscribe({ onGetReports(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun onGetReports(
        resp: BaseResponse<ArrayList<ReportModel>>
    ) {
        if (super.onStatusFalse(resp, true)) return

        if (resp.data?.size != 0) {
            resp.data?.let { arReasons.addAll(it) }
            reportPostDialog()
        }
    }

    fun addView(flowLayout: FlowLayout) {
        if (flowLayout.childCount != 0) {
            flowLayout.removeAllViews()
        }
        var prevPos = 0
        var pos = 0

        for (i in 0 until arReasons.size) {
            val data = arReasons[i]
            val v = LayoutInflater.from(flowLayout.context)
                .inflate(R.layout.flow_report, flowLayout, false) as LinearLayout

            val newDrawable: Drawable =
                DrawableCompat.wrap(resources.getDrawable(R.drawable.bg_btn_radius_twenty))
            DrawableCompat.setTint(newDrawable, resources.getColor(R.color.colorInvitesBg))
            v.llFlowLayout?.background = newDrawable

            v.tvReasonName.text = data.report_name
            v.llFlowLayout.setOnClickListener {
                pos = i
                reasonId = data.id.toInt()
                reasonName = data.report_name
                flowLayout.getChildAt(prevPos).background = newDrawable
                flowLayout.getChildAt(pos).background =
                    resources.getDrawable(R.drawable.bg_btn_blue_radius_twenty)
                prevPos = pos
            }
            flowLayout.addView(v, flowLayout.childCount)
        }
    }

    private fun reportPostDialog() {
        try {
            reportPostdialog = Dialog(this)
            reportPostdialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            reportPostdialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            reportPostdialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            reportPostdialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            reportPostdialog.setContentView(R.layout.dialog_reportpost)
            reportPostdialog.ivReportDilogClose.setOnClickListener(this)
            reportPostdialog.setCanceledOnTouchOutside(false)
            reportPostdialog.window?.attributes?.windowAnimations = R.style.DialogStyle
            reportPostdialog.tvReportPostSend.setOnClickListener(this)

            addView(reportPostdialog.flowLayout)

            val lp = WindowManager.LayoutParams()
            val window = reportPostdialog.window
            lp.copyFrom(window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = lp
            // commentsDialog.getWindow().setCallback(windowCallback);
            reportPostdialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sharePostApi() {
        val request = HashMap<String, Any>()
        val service = HashMap<String, Any>()
        val data = HashMap<String, Any>()
        val auth = HashMap<String, Any>()
        data[getString(R.string.param_pie_id)] = pieId
        auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
        auth[getString(R.string.param_token)] = pref.getToken()
        request[getString(R.string.data)] = data
        service[getString(R.string.service)] = getString(R.string.service_pies_share)
        service[getString(R.string.request)] = request
        service[getString(R.string.auth)] = auth
        callApi(requestInterface.sharePost(service), true)
            ?.subscribe({
                onShareReponse(it)
            }) {
                onResponseFailure(it, true)
            }
            ?.let { mCompositeDisposable.add(it) }

    }

    private fun onShareReponse(resp: BaseResponse<PostModel>) {
        if (super.onStatusFalse(resp, true)) return
        resp.data?.let {
            sneakerError(this,getString(R.string.pie_shared_succ))
        }

    }


    private fun deletePie() {
        if (AppGlobal.isNetworkConnected(this)) run {
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_pie_id)] = pieId

            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_post_pies_delete)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.getPies(service), true)
                ?.subscribe({ onDeletePie(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun onDeletePie(
        resp: BaseResponse<ArrayList<PostModel>>
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return
        val bundle = Bundle()
        bundle.putInt(ARG_DETAIL_DELETE, position)
        RxBus.publish(Bundle(bundle))
        sneakerSuccess(this, resp.message)
    }

    private fun postLike() {
        if (AppGlobal.isNetworkConnected(this)) run {
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_pie_id)] = pieId

            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_post_pies_likes)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.postLike(service), false)
                ?.subscribe({
                    onPostLike(it)
                }) {
                    onResponseFailureLikePost(it, true)
                }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun onResponseFailureLikePost(
        throwable: Throwable,
        vararg doHideLoader: Boolean
    ) {
        onResponseFailure(throwable, doHideLoader[0])
    }

    private fun onPostLike(
        resp: BaseResponse<LikePost>
    ) {
        if (super.onStatusFalse(resp, true)) return
        resp.data?.let {
            tvTotalLike.text = it.count.toString()
            val bundle = Bundle()
            bundle.putInt(ARG_DETAIL_UPDATE, position)
            bundle.putString(ARG_DETAIL_LIKE, it.count.toString())
            bundle.putString(ARG_ISLIKE,tvLikes.tag.toString())
            RxBus.publish(Bundle(bundle))
        }
        // AppLogger.e("tag",resp)
    }

    override fun onItemViewClick(v: View, parentPosition: Int, childPosition: Int) {
        when (v.id) {
            R.id.llReply -> {
                val comment = commentsAdapter.getParentItem(parentPosition)
                llReplyLbl.visibility = View.VISIBLE
                tvSend.tag = parentPosition
                llReplyLbl.tag = comment.id
                replyId = comment.id!!
                tvReplyLbl.text = getString(R.string.replying_to, comment.first_name)
            }
        }
    }

    private fun postComment(
        parentId: String,
        pie_id: String,
        pos1: Int,
        comment: String,
        isCommentReply: Boolean
    ) {
        if (AppGlobal.isNetworkConnected(this)) run {
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_pie_id)] = pie_id
            data[getString(R.string.param_parent_id)] = parentId
            data[getString(R.string.param_comment)] = comment.trim()
            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()
            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_post_pies_comment)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.postComments(service), true)
                ?.subscribe({
                    onPostComment(it, pos1, isCommentReply)
                }) {
                    onResponseFailure(it, true)
                }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun onPostComment(resp: BaseResponse<PostComment>, pos: Int, isCommentReply: Boolean) {
        if (super.onStatusFalse(resp, true)) return
        resp.data?.let {
            var totalComment = tvComments.text.toString().toInt() + 1
            tvComments.text = totalComment.toString()
            val bundle = Bundle()
            bundle.putInt(ARG_DETAIL_COMMENT, totalComment)
            bundle.putInt(ARG_POSITION, position)
            bundle.putBoolean(ARG_ISREPLY, isCommentReply)
            RxBus.publish(bundle)

            if (isCommentReply) {
                edReplyComment.setText("")
                llNodataFound?.visibility = View.GONE
                rvComments?.visibility = View.VISIBLE
                llReplyLbl.visibility = View.GONE
                commentsAdapter.addChildItem(
                    pos, SubComment(
                        id = it.id,
                        comment = it.comment,
                        creation_datetime = it.creation_datetime,
                        first_name = it.first_name,
                        last_name = it.last_name,
                        parent_id = it.parent_id,
                        pie_id = it.pie_id,
                        post_at = it.post_at,
                        profile_pic = it.profile_pic,
                        user_id = it.user_id
                    )
                )
                commentsAdapter.expandParent(pos)
            } else {
                edReplyComment.setText("")
                llNodataFound?.visibility = View.GONE
                rvComments?.visibility = View.VISIBLE
                llReplyLbl.visibility = View.GONE

                rvComments.smoothScrollToPosition(0)

                it.let {
                    commentsAdapter.addWholeItem(
                        CommentModel(
                            id = it.id,
                            comment = it.comment,
                            creation_datetime = it.creation_datetime,
                            first_name = it.first_name,
                            last_name = it.last_name,
                            parent_id = it.parent_id,
                            pie_id = it.pie_id,
                            post_at = it.post_at,
                            profile_pic = it.profile_pic,
                            user_id = it.user_id
                        )
                    )
                }

            }
        }
    }

}
