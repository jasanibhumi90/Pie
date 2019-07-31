package com.pie.ui.pie

import android.graphics.Color
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.pie.R
import com.pie.model.PostModel
import com.pie.ui.base.BaseAdapter
import kotlinx.android.synthetic.main.listitem_home.view.*
import tcking.github.com.giraffeplayer2.VideoInfo


class PieAdapter(
    layout: Int,
    private val clickListener: View.OnClickListener, val type: String
) : BaseAdapter<PostModel>(layout), BaseAdapter.OnBind<PostModel> {

    init {
        setOnBinding(this)
    }

    override fun onBind(view: View, position: Int, data: PostModel) {
        view.run {
            llOne.visibility = View.GONE
            ivImage1.visibility = View.GONE
            ivImage2.visibility = View.GONE

            llTwo.visibility = View.GONE
            ivImage3.visibility = View.GONE
            ivImage4.visibility = View.GONE
            /*if (data.profile_pic.isNotEmpty()) {
                Glide.with(mContext).load(data.profile_pic).load(ivProfile)
            }*/
            //LoadImage(data.profile_pic,ivProfile)
            Glide.with(mContext).load(data.profile_pic)
                .apply(RequestOptions().placeholder(R.drawable.profile_pic)).into(ivProfile)
            tvUserName.text = (data.first_name + " " + data.last_name)
            tvTime.text = data.post_at
            tvPostDesc.text = data.pies_text

            rlView.setOnClickListener(clickListener)
            rlView.tag = position

            ivImage2.setOnClickListener(clickListener)
            ivImage2.tag = position

            ivImage1.setOnClickListener(clickListener)
            ivImage1.tag = position

            ivImage3.setOnClickListener(clickListener)
            ivImage3.tag = position

            ivImage4.setOnClickListener(clickListener)
            ivImage4.tag = position
            if (data.pies_type == "image") {
                rlView.visibility = View.GONE
                if (!data.pies_media_url.isNullOrEmpty() && data.pies_media_url?.size!! > 0) {
                    if (data.pies_media_url?.size!! == 1) {
                        llOne.visibility = View.VISIBLE

                        ivImage1.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![0], ivImage1)
                        //   Glide.with(mContext).load(data.pies_media_url!![0]).into(ivImage1)

                    } else if (data.pies_media_url?.size!! == 2) {
                        llOne.visibility = View.VISIBLE

                        ivImage1.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![0], ivImage1)
                        //  Glide.with(mContext).load(data.pies_media_url!![0]).into(ivImage1)

                        ivImage2.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![1], ivImage2)
                        //   Glide.with(mContext).load(data.pies_media_url!![1]).into(ivImage2)

                    } else if (data.pies_media_url?.size!! == 3) {
                        llOne.visibility = View.VISIBLE
                        llTwo.visibility = View.VISIBLE

                        ivImage1.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![0], ivImage1)
                        // Glide.with(mContext).load(data.pies_media_url!![0]).into(ivImage1)

                        ivImage2.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![1], ivImage2)
                        // Glide.with(mContext).load(data.pies_media_url!![1]).into(ivImage2)


                        ivImage3.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![2], ivImage3)
                        //Glide.with(mContext).load(data.pies_media_url!![2]).into(ivImage3)

                    } else if (data.pies_media_url?.size!! == 4) {
                        llOne.visibility = View.VISIBLE
                        llTwo.visibility = View.VISIBLE

                        ivImage1.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![0], ivImage1)
                        //  Glide.with(mContext).load(data.pies_media_url!![0]).into(ivImage1)

                        ivImage2.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![1], ivImage2)
                        //  Glide.with(mContext).load(data.pies_media_url!![1]).into(ivImage2)


                        ivImage3.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![2], ivImage3)
                        //  Glide.with(mContext).load(data.pies_media_url!![2]).into(ivImage3)


                        ivImage4.visibility = View.VISIBLE
                        LoadImage(data.pies_media_url!![3], ivImage4)
                        //Glide.with(mContext).load(data.pies_media_url!![3]).into(ivImage4)

                    }


                } else {

                }
            } else if (data.pies_type == "video") {
                rlView.visibility = View.GONE
                data.pies_media_url?.let {
                    if (it.size != 0) {
                        rlView.visibility = View.VISIBLE
                        Glide.with(mContext).load(it[0]).into(video_view.coverView)
                        video_view.setVideoPath(it[0]).setFingerprint(position)


                    }
                }

            } else {
                rlView.visibility = View.GONE
                llOne.visibility = View.GONE
                llTwo.visibility = View.GONE
            }


            tvLikes.text = data.likes
            tvComments.text = data.comments
            tvViews.text = data.view_count

            if (data.like_flag == "0")
                ivLike.setImageDrawable(resources.getDrawable( R.drawable.p_heart))
            else
                ivLike.setImageDrawable(resources.getDrawable( R.drawable.p_heart_liked))

            ivLike.setOnClickListener(clickListener)
            ivLike.tag = position
            ivLike.setTag(R.id.TYPE, type)

            tvLikes.setOnClickListener(clickListener)
            tvLikes.tag = position

            cvPost.setOnClickListener(clickListener)
            cvPost.tag = position
            if (data.like_flag == "0")
                tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.p_heart, 0, 0, 0)
            else
                tvLikes.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.p_heart_liked,
                    0,
                    0,
                    0
                )
            tvLikes.setOnClickListener(clickListener)
            tvLikes.tag = position
            tvLikes.setTag(R.id.TYPE, type)
            cvPost.setOnClickListener(clickListener)
            cvPost.tag = position

            ivMenu.setOnClickListener(clickListener)
            ivMenu.tag = position

            tvComments.setOnClickListener(clickListener)
            tvComments.tag = position

            ivProfile.setOnClickListener(clickListener)
            ivProfile.tag=position


            tvComments.setOnClickListener(clickListener)
            tvComments.tag = position

            ivPlay.setOnClickListener {
                ivPlay.visibility=View.GONE
                ivPlay.visibility=View.GONE
                ivPlay.visibility=View.GONE
                ivPlay.visibility=View.GONE
                video_view.player.start()
            }
        }
    }

}
