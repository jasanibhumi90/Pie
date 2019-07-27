package com.pie.ui.home.comment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import com.bignerdranch.expandablerecyclerview.ChildViewHolder
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter
import com.bignerdranch.expandablerecyclerview.ParentViewHolder
import com.bignerdranch.expandablerecyclerview.model.Parent
import com.bumptech.glide.Glide
import com.pie.R
import com.pie.model.CommentModel
import com.pie.model.SubComment
import com.pie.ui.base.BaseDiffCallback
import kotlinx.android.synthetic.main.raw_comment_list.view.*
import kotlinx.android.synthetic.main.raw_comment_list.view.tvComments
import kotlinx.android.synthetic.main.raw_comment_list.view.tvTime
import kotlinx.android.synthetic.main.raw_comment_list.view.tvUserName
import kotlinx.android.synthetic.main.raw_comment_reply_list.view.*

class CommentsAdapter2(val context: Context, parentList: List<CommentModel>) :
        ExpandableRecyclerAdapter<CommentModel, SubComment, CommentsAdapter2.CommentHolder,
                CommentsAdapter2.CommentReplyHolder>(parentList),
        BaseDiffCallback.ItemCheckerInterface<CommentModel>,
        BaseDiffCallback.ItemContentsCheckerInterface<CommentModel> {
    private var mOnItemViewClickListener: OnItemViewClickListener? = null
    private var mInflater: LayoutInflater? = null

    init {
        mInflater = LayoutInflater.from(context)
    }

    @UiThread
    override fun onCreateParentViewHolder(
            parentViewGroup: ViewGroup,
            viewType: Int
    ): CommentHolder {
        return CommentHolder(
                mInflater!!.inflate(R.layout.raw_comment_list, parentViewGroup, false),
                mOnItemViewClickListener
        )
    }

    @UiThread
    override fun onCreateChildViewHolder(
            childViewGroup: ViewGroup,
            viewType: Int
    ): CommentReplyHolder {
        return CommentReplyHolder(
                mInflater!!.inflate(R.layout.raw_comment_reply_list, childViewGroup, false),
                mOnItemViewClickListener
        )
    }

    @UiThread
    override fun onBindParentViewHolder(
            recipeViewHolder: CommentHolder,
            parentPosition: Int,
            comments: CommentModel
    ) {
        recipeViewHolder.bind(comments)
    }

    @UiThread
    override fun onBindChildViewHolder(
            ingredientViewHolder: CommentReplyHolder,
            parentPosition: Int,
            childPosition: Int,
            commentReply: SubComment
    ) {
        ingredientViewHolder.bind(commentReply)
    }

    @UiThread
    class CommentHolder(
            itemView: View,
            private val mOnItemViewClickListener: OnItemViewClickListener?
    ) : ParentViewHolder<Parent<Any>, Any>(itemView) {
        fun bind(comments: CommentModel) {
            itemView.run {
                Glide.with(context).load(comments.profile_pic).placeholder(R.drawable.profile_pic).into(ivProfilepic)
                tvUserName.text = comments.first_name
                tvComments.text = comments.comment
                tvTime.text = comments.post_at
                llReply.setOnClickListener {
                    mOnItemViewClickListener?.onItemViewClick(it, parentAdapterPosition, -1)
                }

                if(comments.subcomment.size>0){
                    tvViewReply.text=resources.getString(R.string.view_reply,comments.subcomment.size.toString())
                    llViewReply.setOnClickListener {
                        llViewReply.visibility=View.GONE
                        expandView()
                    }
                    llViewReply.visibility=View.VISIBLE
                }else{
                    llViewReply.visibility=View.GONE
                }

            }
        }

        override fun setExpanded(expanded: Boolean) {


        }
        override fun onExpansionToggled(expanded: Boolean) {

        }
    }

    @UiThread
    class CommentReplyHolder(itemView: View, private val mOnItemViewClickListener: OnItemViewClickListener?) : ChildViewHolder<Any>(itemView) {
        fun bind(commentReply: SubComment) {

            itemView.run {
                Glide.with(context).load(commentReply.profile_pic).placeholder(R.drawable.profile_pic).into(ivProfilepicReply)
                tvUserNameReply.text = commentReply.first_name
                tvCommentsReply.text = commentReply.comment
                tvTimeReply.text = commentReply.post_at
             //   subComment.text=resources.getString(R.string.view_reply,parentAdapterPosition)
            }
        }
    }
    fun getSubCommentSize(parentPosition: Int):Int{
        return parentList[parentPosition].subcomment.size
    }

    fun getParentItem(parentPosition: Int): CommentModel {
        return parentList[parentPosition]
    }

    fun getChildItem(parentPosition: Int, childPosition: Int): SubComment {
        return parentList[parentPosition].subcomment[childPosition]
    }

    fun updateParent(parentPosition: Int, comment: CommentModel) {
        parentList[parentPosition] = comment
        notifyParentChanged(parentPosition)
    }

    fun updateChild(parentPosition: Int, childPosition: Int, commentReply: SubComment) {
        parentList[parentPosition].subcomment[childPosition] = commentReply
        notifyChildChanged(parentPosition, childPosition)
    }

    fun addWholeItem(comments: CommentModel) {
        parentList.add(0, comments)
        notifyParentInserted(0)
    }

    fun addChildItem(parentPosition: Int, commentReply: SubComment) {
        parentList[parentPosition].subcomment.add(commentReply)
        notifyChildInserted(parentPosition, parentList[parentPosition].subcomment.size - 1)
    }

    fun addAll(comments: ArrayList<CommentModel>) {
        parentList.clear()
        notifyParentRangeInserted(0, comments.size)
    }

    fun appendAll(comments: ArrayList<CommentModel>) {
        val oldSize = parentList.size
        parentList.addAll(comments)
        notifyParentRangeInserted(oldSize, comments.size)
    }

    fun getLastItem(): CommentModel? {
        if (parentList.isNotEmpty()) {
            return parentList[parentList.size - 1]
        }
        return null
    }

    override fun isItemSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun isItemContentsSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
        return true
    }

    //region onItemViewClick
    interface OnItemViewClickListener {
        fun onItemViewClick(v: View, parentPosition: Int, childPosition: Int)
    }

    fun setOnItemViewClickListener(onItemViewClickListener: OnItemViewClickListener) {
        mOnItemViewClickListener = onItemViewClickListener
    }

    fun getAllItem(): List<CommentModel> {
        return parentList
    }
}
//endregion onItemViewClick
