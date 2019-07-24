package com.pie.ui.piedetail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.pie.PieApp
import com.pie.R
import com.pie.model.*
import com.pie.ui.base.BaseActivity
import com.pie.ui.home.comment.CommentsAdapter2
import com.pie.utils.AppConstant
import com.pie.utils.AppConstant.Companion.ARG_PIE_ID
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.activity_pie_detail.*
import kotlinx.android.synthetic.main.toolbar_common.*
import java.util.*
import kotlin.collections.ArrayList

class PieDetailActivity : BaseActivity(), View.OnClickListener,
    CommentsAdapter2.OnItemViewClickListener {
    var commentsAdapter: CommentsAdapter2 =
        CommentsAdapter2(PieApp.getInstance(), ArrayList())
    var isCommentReply: Boolean = false
    private var replyId:String="0"
    private var parentId:String=""
    private var pieId:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_detail)
        if (intent.hasExtra(ARG_PIE_ID)) {
            init()
            clickListener()
            getPieDetail(intent.getIntExtra(ARG_PIE_ID, 0))
        }

    }

    private fun clickListener() {

    }

    fun init() {
        tvTitle.text=getString(R.string.pie)
        setCommentAdapter(ArrayList())
    }

    private fun setCommentAdapter(comments: List<CommentModel>) {
        rvComments.layoutManager = LinearLayoutManager(this)
        commentsAdapter = CommentsAdapter2(this, comments)
        commentsAdapter.expandAllParents()
        commentsAdapter.setOnItemViewClickListener(this)
        rvComments.adapter = commentsAdapter
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
        service[getString(R.string.service)] = getString(R.string.service_get_pies)
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

    private fun onGetPieResponse(resp: BaseResponse<ArrayList<GetPieView>>) {
        if (super.onStatusFalse(resp, true)) return
        resp.data?.let {
            it[0].let {
                consPieDetail.visibility = View.VISIBLE
                tvName.text = it.first_name
                tvUserName.text = it.last_name
                tvTime.text = it.post_at
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
                        setImage(ivImage1, it.pies_media_url[1])
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

                    tvTotalLike.text = it.likes
                    tvComments.text = it.comments
                    tvViews.text = it.view_count

                    if (it.like_flag == "0") {
                        tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.p_heart, 0, 0, 0)
                    }else {
                        tvLikes.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.p_heart_liked,
                            0,
                            0,
                            0
                        )
                    }
                }
                if (it.comment_list.size > 0) {
                    rvComments.visibility = View.VISIBLE
                    llNodataFound.visibility = View.GONE
                    commentsAdapter.appendAll(it.comment_list)
                } else {
                    rvComments.visibility = View.GONE
                    llNodataFound.visibility = View.VISIBLE
                }
            }

        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.tvSend -> {
                val position = p0.tag as Int
                isCommentReply = llReplyLbl.visibility == View.VISIBLE
                if(llReplyLbl.visibility==View.VISIBLE)
                    postComment(replyId,pieId, position, edReplyComment.text.toString().trim(), isCommentReply)
                else postComment(parentId, pieId, position, edReplyComment.text.toString().trim(), isCommentReply)
            }
        }
    }

    override fun onItemViewClick(v: View, parentPosition: Int, childPosition: Int) {
        when (v.id) {
            R.id.llReply -> {
                val comment = commentsAdapter.getParentItem(parentPosition)
                llReplyLbl.visibility = View.VISIBLE
                tvSend.tag = parentPosition
                llReplyLbl.tag = comment.id
                replyId=comment.id!!
                tvReplyLbl.text = getString(R.string.replying_to, comment.first_name)
            }
        }

    }

    private fun postComment(parentId: String, pie_id: String, pos1: Int, comment: String, isCommentReply: Boolean) {
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
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onPostComment(resp: BaseResponse<PostComment>, pos: Int, isCommentReply: Boolean) {
        if (super.onStatusFalse(resp, true)) return
        AppLogger.e("tag", "resp.." + gson.toJson(resp))
       // stopAnimation(commentsdialog.shimmerViewContainer)
        resp.data?.let {
            try {

                var totalComment = tvComments.text.toString().toInt() + 1
                tvComments.text = totalComment.toString()
            } catch (e: Exception) {
                AppLogger.e("tag", "e.." + e)
            }
            if (isCommentReply) {
                edReplyComment.setText("")
                llNodataFound?.visibility = View.GONE
                rvComments?.visibility = View.VISIBLE
                llReplyLbl.visibility = View.GONE
                commentsAdapter.addChildItem(pos, SubComment(id = it.id, comment = it.comment, creation_datetime = it.creation_datetime, first_name = it.first_name,
                    last_name = it.last_name, parent_id = it.parent_id, pie_id = it.pie_id, post_at = it.post_at, profile_pic = it.profile_pic, user_id = it.user_id)
                )
            } else {
                edReplyComment.setText("")
                llNodataFound?.visibility = View.GONE
                rvComments?.visibility = View.VISIBLE
                llReplyLbl.visibility = View.GONE
                rvComments.smoothScrollToPosition(0)

                it.let {
                    commentsAdapter.addWholeItem(
                        CommentModel(id = it.id, comment = it.comment, creation_datetime = it.creation_datetime, first_name = it.first_name,
                            last_name = it.last_name, parent_id = it.parent_id, pie_id = it.pie_id, post_at = it.post_at, profile_pic = it.profile_pic, user_id = it.user_id)
                    )
                }

            }
        }
    }

}
