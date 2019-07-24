package com.pie.ui.pie

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.pie.R

import java.util.ArrayList

class SlidingImage_Adapter(private val context: Context, private val IMAGES: ArrayList<String>) : PagerAdapter() {
    private val inflater: LayoutInflater


    init {
        inflater = LayoutInflater.from(context)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return IMAGES.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = inflater.inflate(R.layout.listitem_post_image, view, false)!!

        val imageView = imageLayout
            .findViewById<View>(R.id.ivImage) as ImageView

        Glide.with(context).load(IMAGES.get(position)).into(imageView)

        view.addView(imageLayout, 0)

        return imageLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun saveState(): Parcelable? {
        return null
    }


}