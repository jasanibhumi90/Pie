package com.pie.ui.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.irozon.sneaker.widget.RoundedImageView
import com.pie.PieApp
import com.pie.data.prefs.AppPreferencesHelper


abstract class BaseAdapter<T>(val layout: Int = 0) : RecyclerView.Adapter<BaseAdapter.ViewHolder>() {
    val TAG = javaClass.canonicalName
    val list = ArrayList<T>()
    private var mOnBind: OnBind<T>? = null
    private var mOnPayloadBind: OnPayloadBind<T>? = null
    var mContext: Context = PieApp.getInstance()
    var pref: AppPreferencesHelper = PieApp.getInstance().getAppPreferencesHelper()
    fun setOnBinding(mOnBind: OnBind<T>) {
        this.mOnBind = mOnBind
    }

    fun setOnPayloadBinding(mOnPayloadBind: OnPayloadBind<T>) {
        this.mOnPayloadBind = mOnPayloadBind
    }

    interface OnBind<in T> {
        fun onBind(view: View, position: Int, item: T)
    }

    interface OnPayloadBind<in T> {
        fun onPayloadBind(view: View, position: Int, item: T, payloads: MutableList<Any>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(v, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            mOnPayloadBind?.onPayloadBind(holder.getBindView(), position, list[position], payloads)
        }
    }

    fun LoadImage(image:String,imageView:com.makeramen.roundedimageview.RoundedImageView){
        Glide.with(mContext)
            .asBitmap()
            .load(image)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mOnBind?.onBind(holder.getBindView(), position, list[position])
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val view: View, val context: Context) : RecyclerView.ViewHolder(view) {
        fun getBindView(): View {
            return view
        }
    }


    fun removeItem(position: Int) {
        list.removeAt(position)
        notifyDataSetChanged()
    }

    fun removePie(userId:Int){

    }


    fun updateItem(position: Int, item: T) {
        list[position] = item
        notifyDataSetChanged()
    }

    fun addAtFirst(item: T) {
        list.add(0, item)
        notifyDataSetChanged()
    }

    fun addAll(dataList: ArrayList<T>) {
        list.clear()
        list.addAll(dataList)
        notifyDataSetChanged()
    }
   fun appendAll(dataList: ArrayList<T>) {

        list.addAll(dataList)
        notifyDataSetChanged()
    }

    fun add(data: T) {
        list.add(data)
        notifyDataSetChanged()
    }


    fun getAll(): ArrayList<T> {
        return list
    }

    fun getItem(position: Int): T {
        return list[position]
    }
}
