package com.pie.ui.pie

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.tcking.giraffecompressor.GiraffeCompressor
import com.pie.PieApp
import com.pie.R
import com.pie.model.*
import com.pie.ui.base.BaseFragment
import com.pie.ui.createpie.CreatePieActivity
import com.pie.ui.editpie.EditPieActivity
import com.pie.ui.home.comment.CommentsAdapter2
import com.pie.ui.piedetail.PieDetailActivity
import com.pie.utils.*
import com.pie.utils.AppConstant.Companion.ARG_DATA
import com.pie.utils.AppConstant.Companion.ARG_PIE_DATA
import com.pie.utils.AppConstant.Companion.TYPE_LIKE_POST
import kotlinx.android.synthetic.main.dialog_alert.*
import kotlinx.android.synthetic.main.dialog_comments.*
import kotlinx.android.synthetic.main.dialog_reportpost.*
import kotlinx.android.synthetic.main.flow_report.view.*
import kotlinx.android.synthetic.main.fragment_pie.*
import org.apmem.tools.layouts.FlowLayout
import org.jetbrains.anko.startActivity
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set


class PieFragment : BaseFragment(), View.OnClickListener,CommentsAdapter2.OnItemViewClickListener,Pagination.OnLoadMore {
    val REQUEST_CODE_CREATE_PIE = 100
    val REQUEST_CODE_EDIT_PIE = 101
    private lateinit var reportPostdialog: Dialog
    val arReasons: ArrayList<ReportModel> = ArrayList()
    private lateinit var pieAdapter: PieAdapter
    private var reasonId = 0
    private var reasonName = ""
    var isCommentReply: Boolean = false
    var isDone=false
    var isLoading = false
    val isLastPage = false
    private lateinit var pagination: Pagination
    private lateinit var commentsdialog: Dialog
    var commentsAdapter: CommentsAdapter2 = CommentsAdapter2(PieApp.getInstance(), ArrayList())
    private var replyId:String="0"
    private var pageNo:Int=0
    val arPosts: ArrayList<String> = ArrayList()
    private lateinit var productImageSliderAdapter: ProductImageSliderAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GiraffeCompressor.init(context)
        ivCreatePie.setOnClickListener(this)

       /*string*/
        pieAdapter = PieAdapter(R.layout.listitem_home, this,TYPE_LIKE_POST)
        val linearLayoutManager = LinearLayoutManager(activity)
        rvPosts.run {
            layoutManager = linearLayoutManager
            adapter = pieAdapter
        }

        pagination = Pagination(rvPosts, this)
        pagination.setPaginationListener()
        pagination.setProgressView(aviPage)


        getPies(true)
    }
    override fun onLoadMore() {
        pageNo=pageNo+1
        getPies(false)

    }

    private fun isValid(): Boolean {
        if (reasonName.isEmpty()) {
            AppGlobal.alertDialog(mActivity, resources.getString(R.string.error_reason))
            return false
        } else if (reasonName.toLowerCase() == "other".toLowerCase() && reportPostdialog.edDescription.text.toString().isEmpty()) {
            AppGlobal.alertDialog(mActivity, resources.getString(R.string.error_desc))
            return false
        }
        return true
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivImage1 -> {
                val pos = p0.tag as Int
                activity?.startActivity<ImageSliderActivity>(AppConstant.EXTRA_IMAGE_DATA to pieAdapter.getItem(pos).pies_media_url,
                  AppConstant.ARG_POS to 0)

            }
            R.id.ivImage2 -> {
                val pos = p0.tag as Int
                activity?.startActivity<ImageSliderActivity>(AppConstant.EXTRA_IMAGE_DATA to pieAdapter.getItem(pos).pies_media_url,
                    AppConstant.ARG_POS to 1)

            }
            R.id.ivImage3 -> {
                val pos = p0.tag as Int
                activity?.startActivity<ImageSliderActivity>(AppConstant.EXTRA_IMAGE_DATA to pieAdapter.getItem(pos).pies_media_url,
                    AppConstant.ARG_POS to 2)

            }
            R.id.ivImage4 -> {
                val pos = p0.tag as Int
                activity?.startActivity<ImageSliderActivity>(AppConstant.EXTRA_IMAGE_DATA to pieAdapter.getItem(pos).pies_media_url,
                    AppConstant.ARG_POS to 3)

            }


            R.id.ivCreatePie -> {
                val intent = Intent(activity!!, CreatePieActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_CREATE_PIE)
            }

            R.id.ivMenu -> {
                val pos = p0.tag as Int
                openMenu(pos,p0,pieAdapter.getItem(pos).user_id)

            }
            R.id.tvLikes -> {
                if (pref.isLogin()) {
                    val pos = p0.tag as Int
                    val data = getDataByType(p0.getTag(R.id.TYPE).toString(), pos).clone()
                    val newItem = getDataByType(p0.getTag(R.id.TYPE).toString(), pos)
                    if (newItem.like_flag == "0")
                        newItem.like_flag = "1"
                    else
                        newItem.like_flag = "0"

                    postLike(pieAdapter.getItem(pos).id, pos, data)
                    updateItemByType(newItem, p0.getTag(R.id.TYPE).toString(), pos)
                } else {
                    AppGlobal.alertDialog(activity, getString(R.string.loginfirst_error))
                }
            }

            R.id.tvComments -> {
                val pos = p0.tag as Int
                getPieComments(pos)
            }
            R.id.ivReportDilogClose -> {
                reportPostdialog.dismiss()
            }
            R.id.tvSend -> {
                val position = p0.tag as Int
                isCommentReply = commentsdialog.llReplyLbl.visibility == View.VISIBLE
                if(commentsdialog.llReplyLbl.visibility==View.VISIBLE)
                    postComment(replyId, pieAdapter.getItem(position).id, position, commentsdialog.edReplyComment.text.toString().trim(), isCommentReply)
                else postComment(pieAdapter.getItem(position).parent_id, pieAdapter.getItem(position).id, position, commentsdialog.edReplyComment.text.toString().trim(), isCommentReply)
            }
            R.id.cvPost->{
                val id=pieAdapter.getItem(p0.tag as Int).id
                activity?.startActivity<PieDetailActivity>(AppConstant.ARG_PIE_ID to id)
            }
            R.id.rlView -> {
                val pos=p0.tag as Int
                if(pieAdapter.getItem(pos).pies_media_url?.size!=0){
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(pieAdapter.getItem(pos).pies_media_url!![0]), "video/mp4")
                    startActivity(intent)
                }
            }
            R.id.tvReportPostSend -> {
                if (isValid()) {

                    val pos: Int = p0.tag as Int
                    reportPost(
                        pos,
                        pieAdapter.getItem(pos).id,
                        reasonId,
                        reportPostdialog.edDescription.text.toString()
                    )
                }
            }
            R.id.ivDilogClose -> {
                commentsdialog.dismiss()
            }
        }
    }

    private fun updateItemByType(data: PostModel, type: String, pos: Int) {
        when (type) {
            TYPE_LIKE_POST -> pieAdapter.updateItem(pos, data)
        }
    }
    private fun openMenu(position: Int,v: View, userId: String) {
        val popup = PopupMenu(this.activity!!, v)

        popup.inflate(R.menu.option_menu)
        if (pref.getLoginData()?.user_id == userId) {
            if(pieAdapter.getItem(position).pies_type=="image") {
                popup.menu.findItem(R.id.menu1).isVisible = true
            }else if(pieAdapter.getItem(position).pies_type=="video") {
                popup.menu.findItem(R.id.menu1).isVisible = false
            }else{
                popup.menu.findItem(R.id.menu1).isVisible = true
            }
            popup.menu.findItem(R.id.sharePost).isVisible=false
            popup.menu.findItem(R.id.menu1).title = resources.getString(R.string.menu_editpost)
            popup.menu.findItem(R.id.menu2).title = resources.getString(R.string.menu_deletepost)
        } else {
            popup.menu.findItem(R.id.menu1).isVisible=false
            popup.menu.findItem(R.id.sharePost).isVisible=true
            popup.menu.findItem(R.id.menu2).title = resources.getString(R.string.menu_reportpost)
        }
        popup.setOnMenuItemClickListener { item ->
           val position = v.tag as Int

            when (item.itemId) {
                R.id.menu1 -> {
                    val intent = Intent(activity!!, EditPieActivity::class.java)
                    intent.putExtra(ARG_PIE_DATA , pieAdapter.getItem(position))
                    startActivityForResult(intent, REQUEST_CODE_EDIT_PIE)

                }  R.id.menu2 -> {
                    if (pref.getLoginData()?.user_id == userId) {
                         deletePie(position)
                    } else {
                        getReports(position)
                    }
                }
                R.id.sharePost->{
                    shareAlertDialog(position)
                }
            }
            false
        }
        popup.show()
    }

    private fun shareAlertDialog(position: Int) {
        val dialog = Dialog(activity)
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
            sharePostApi(position)
        }
        dialog.show()
    }

    private fun sharePostApi(position: Int) {
        val request = HashMap<String, Any>()
        val service = HashMap<String, Any>()
        val data = HashMap<String, Any>()
        val auth = HashMap<String, Any>()
        data[getString(R.string.param_pie_id)] = pieAdapter.getItem(position).id
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

    private fun onShareReponse(resp: BaseResponse<PostModel>?) {
        if(super.onStatusFalse(resp,true))return
        resp?.data?.let {
            AppLogger.e("tag",gson.toJson(it))
            pieAdapter.addAtFirst(it)
            rvPosts.scrollToPosition(0)
        }

    }

    private fun getPieComments(position: Int) {
        try {
            commentsdialog = Dialog(activity)
            commentsdialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            commentsdialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            commentsdialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            commentsdialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            commentsdialog.setContentView(R.layout.dialog_comments)

            commentsdialog.setCanceledOnTouchOutside(false)
            commentsdialog.window.attributes.windowAnimations = R.style.DialogStyle
            commentsdialog.tvSend.startAnimation(fab_open)
            startAnimation(commentsdialog.shimmerViewContainer)
            commentsdialog.ivDilogClose.setOnClickListener(this)
            commentsdialog.ivReplyClose.setOnClickListener(this)

            setCommentAdapter(activity, ArrayList())
            getComment(pieAdapter.getItem(position).id)
            commentsdialog.tvSend.setOnClickListener(this)
            commentsdialog.ivDilogClose.setOnClickListener(this)
            commentsdialog.tvSend.tag = position
            commentsdialog.rvComments.visibility = View.VISIBLE

            val lp = WindowManager.LayoutParams()
            val window = commentsdialog.window
            lp.copyFrom(window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = lp
            // commentsDialog.getWindow().setCallback(windowCallback);
            commentsdialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun setCommentAdapter(context: Context?, comments: List<CommentModel>) {
        context?.let {
            commentsdialog.rvComments.layoutManager = LinearLayoutManager(context)
            commentsAdapter = CommentsAdapter2(it, comments)
            commentsAdapter.expandAllParents()
            commentsAdapter.setOnItemViewClickListener(this@PieFragment)
            commentsdialog.rvComments.adapter = commentsAdapter
        }
    }

    //region getComments
    private fun getComment(id: String) {
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
                    onGetCommentResponse(it)
                }) {
                    onResponseFailure(it, true)
                }
                ?.let { mCompositeDisposable.add(it) }
    }

    private fun onGetCommentResponse(resp: BaseResponse<ArrayList<GetPieView>>) {
        if (super.onStatusFalse(resp, true)) return
        stopAnimation(commentsdialog.shimmerViewContainer)
        resp.data?.let {
            it[0].let {
            if (it.comment_list.size > 0) {
                commentsdialog.rvComments.visibility = View.VISIBLE
                commentsdialog.llNodataFound.visibility = View.GONE
                commentsAdapter.appendAll(it.comment_list)
            } else {
                commentsdialog.rvComments.visibility = View.GONE
                commentsdialog.llNodataFound.visibility = View.VISIBLE
            }
           }
        }
    }

    private fun postComment(parentId: String, pie_id: String, pos1: Int, comment: String, isCommentReply: Boolean) {
        if (AppGlobal.isNetworkConnected(activity!!)) run {
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
            Toast.makeText(activity!!, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun reportPost(position: Int, postId: String, reason: Int, comment: String) {
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
            ?.subscribe({ onGetPostReport(it, position) }) { onResponseFailure(it, true) }
            ?.let { mCompositeDisposable.add(it) }

    }

    private fun onGetPostReport(
        resp: BaseResponse<Any>,
        position: Int
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return
        reportPostdialog.dismiss()
        sneakerSuccess(activity!!, resp.message)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CREATE_PIE -> {
                    AppLogger.e("tag", "createpie++")
                    data?.let {
                        val postModel = it.getSerializableExtra(ARG_DATA) as PostModel?
                        postModel?.let {
                            pieAdapter.addAtFirst(it)
                        }
                    }
                }
                REQUEST_CODE_EDIT_PIE -> {
                    AppLogger.e("tag", "createpie++")
                    data?.let {
                        val postModel = it.getSerializableExtra(ARG_DATA) as PostModel?
                        val pos=pieAdapter.getAll().indexOfFirst { it.id==postModel?.id }
                        if(pos!=-1){
                            postModel?.let { it1 -> pieAdapter.updateItem(pos, it1) }
                        }
                    }
                }
            }

        }
    }

    private fun getPies(doShowLoader:Boolean) {
        if (AppGlobal.isNetworkConnected(activity!!)) run {


            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_offset)] = pageNo.toString()

            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_get_pies)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.getPies(service), doShowLoader)
                ?.subscribe({ onGetPie(it,doShowLoader) }) { onResponseFailure(it, doShowLoader) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(activity!!, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onGetPie(
        resp: BaseResponse<ArrayList<PostModel>>,
        doShowLoader: Boolean
    ) {
        Log.e("tag", "resp" + gson.toJson(resp))
            if (super.onStatusFalse(resp, doShowLoader)) return

        pagination.setItemLoaded()
        resp.data?.let {
            if (it.size!=0) {

                pagination.setItemLoaded()
                if (it.size < 10) {
                    pagination.setLoadMore(false)
                }
                if (pageNo == 0) {
                    pieAdapter.addAll(it)

                    Handler().postDelayed({ pagination.setLoadMore(true) }, 500)

                } else {
                    pieAdapter.appendAll(it)

                }
            } else {

            }
        }

        }



    private fun deletePie(position: Int) {
        if (AppGlobal.isNetworkConnected(activity!!)) run {


            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_pie_id)] = pieAdapter.getItem(position).id

            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_post_pies_delete)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.getPies(service), true)
                ?.subscribe({ onDeletePie(it, position) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(activity!!, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onDeletePie(
        resp: BaseResponse<ArrayList<PostModel>>,
        position: Int
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return
sneakerSuccess(activity!!,resp.message)
        pieAdapter.removeItem(position)
    }


    private fun getReports(position: Int) {
        if (AppGlobal.isNetworkConnected(activity!!)) run {


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
                ?.subscribe({ onGetReports(it, position) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(activity!!, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onGetReports(
        resp: BaseResponse<ArrayList<ReportModel>>,
        position: Int
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return

        if (resp.data?.size != 0) {
            resp.data?.let { arReasons.addAll(it) }
            reportPostDialog(position)
        }
    }

    private fun reportPostDialog(position: Int) {
        try {
            reportPostdialog = Dialog(activity)
            reportPostdialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            reportPostdialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            reportPostdialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            reportPostdialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            reportPostdialog.setContentView(R.layout.dialog_reportpost)
            reportPostdialog.ivReportDilogClose.setOnClickListener(this)
            reportPostdialog.setCanceledOnTouchOutside(false)
            reportPostdialog.window?.attributes?.windowAnimations = R.style.DialogStyle

            reportPostdialog.tvReportPostSend.setOnClickListener(this)

            reportPostdialog.tvReportPostSend.tag = position

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

    private fun onPostComment(resp: BaseResponse<PostComment>, pos: Int, isCommentReply: Boolean) {
        if (super.onStatusFalse(resp, true)) return
        AppLogger.e("tag", "resp.." + gson.toJson(resp))
        stopAnimation(commentsdialog.shimmerViewContainer)
        resp.data?.let {
            try {
                val post = pieAdapter.getItem(pos)
                var totalComment = post.comments.toInt() + 1
                post.comments = totalComment.toString()
                pieAdapter.updateItem(pos, post)
            } catch (e: Exception) {
                AppLogger.e("tag", "e.." + e)
            }
            if (isCommentReply) {
                commentsdialog.edReplyComment.setText("")
                commentsdialog.llNodataFound?.visibility = View.GONE
                commentsdialog.rvComments?.visibility = View.VISIBLE
                commentsdialog.llReplyLbl.visibility = View.GONE
                commentsAdapter.addChildItem(pos, SubComment(id = it.id, comment = it.comment, creation_datetime = it.creation_datetime, first_name = it.first_name,
                        last_name = it.last_name, parent_id = it.parent_id, pie_id = it.pie_id, post_at = it.post_at, profile_pic = it.profile_pic, user_id = it.user_id))
            } else {
                commentsdialog.edReplyComment.setText("")
                commentsdialog.llNodataFound?.visibility = View.GONE
                commentsdialog.rvComments?.visibility = View.VISIBLE
                commentsdialog.llReplyLbl.visibility = View.GONE
                commentsdialog.rvComments.smoothScrollToPosition(0)

                it.let {
                    commentsAdapter.addWholeItem(
                            CommentModel(id = it.id, comment = it.comment, creation_datetime = it.creation_datetime, first_name = it.first_name,
                                    last_name = it.last_name, parent_id = it.parent_id, pie_id = it.pie_id, post_at = it.post_at, profile_pic = it.profile_pic, user_id = it.user_id)
                    )
                }

            }
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

            val newDrawable: Drawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.bg_btn_radius_twenty))
            DrawableCompat.setTint(newDrawable, resources.getColor(R.color.colorInvitesBg))
            v.llFlowLayout?.background = newDrawable

            v.tvReasonName.text = data.report_name
            v.llFlowLayout.setOnClickListener {
                pos = i
                reasonId = data.id.toInt()
                reasonName = data.report_name
                flowLayout.getChildAt(prevPos).background = newDrawable
                flowLayout.getChildAt(pos).background = resources.getDrawable(R.drawable.bg_btn_blue_radius_twenty)
                prevPos = pos
            }
            flowLayout.addView(v, flowLayout.childCount)
        }
    }

    private fun postLike(id: String, pos: Int, postModel: PostModel) {
        if (AppGlobal.isNetworkConnected(activity!!)) run {
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_pie_id)] = id

            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_post_pies_likes)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.postLike(service), false)
                    ?.subscribe({
                        onPostLike(it, pos)
                    }) {
                        onResponseFailureLikePost(pos, postModel, it, true)
                    }
                    ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(activity!!, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onResponseFailureLikePost(
            pos: Int,
            postModel: PostModel,
            throwable: Throwable,
            vararg doHideLoader: Boolean
    ) {
        onResponseFailure(throwable, doHideLoader[0])
        pieAdapter.updateItem(pos, postModel)
    }

    private fun onPostLike(
            resp: BaseResponse<LikePost>,
            pos: Int
    ) {
        if (super.onStatusFalse(resp, true)) return
        resp.data?.let {
            val data = pieAdapter.getItem(pos)
            data.likes = it.count.toString()
            pieAdapter.updateItem(pos, data)

        }
        // AppLogger.e("tag",resp)
    }
    override fun onItemViewClick(v: View, parentPosition: Int, childPosition: Int) {
        when (v.id) {
            R.id.llReply -> {
                val comment = commentsAdapter.getParentItem(parentPosition)
                commentsdialog.llReplyLbl.visibility = View.VISIBLE
                commentsdialog.tvSend.tag = parentPosition
                commentsdialog.llReplyLbl.tag = comment.id
                replyId=comment.id!!
                commentsdialog.tvReplyLbl.text = getString(R.string.replying_to, comment.first_name)
            }
        }
    }

    private fun getDataByType(type: String, pos: Int): PostModel {
        var data: PostModel? = null
        when (type) {
            TYPE_LIKE_POST -> data = pieAdapter.getItem(pos)
            /*  TYPE_OFFER_PRODUCT -> data = offerProductsAdapter.getItem(pos)
              TYPE_DEALS_OF_THE_DAY -> data = dealsOfTheDayAdapter.getItem(pos)*/
        }
        return data!!
    }


}
