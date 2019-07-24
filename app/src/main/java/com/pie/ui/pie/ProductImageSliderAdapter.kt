package com.pie.ui.pie

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.pie.PieApp
import com.pie.R
import kotlinx.android.synthetic.main.item_slider_image.view.*


class ProductImageSliderAdapter(private val arImages: ArrayList<String>, val onClickListener: View.OnClickListener) :
    PagerAdapter() {
    internal var mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater = PieApp.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val itemView = mLayoutInflater.inflate(R.layout.item_slider_image, container, false)
        val imageView = itemView.findViewById(R.id.ivImage) as ImageView
        Glide.with(PieApp.getInstance()).load(arImages[position]).into(imageView)
        container.addView(itemView)
        itemView.llImage.setOnClickListener(onClickListener)
        itemView.llImage.tag = position
        return itemView
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return arImages.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}