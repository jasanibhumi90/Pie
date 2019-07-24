package com.pie.ui.base


import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil

class BaseDiffCallback<T>(oldEmployeeList: List<T>, newEmployeeList: List<T>) : DiffUtil.Callback() {
    private val mOldEmployeeList: List<T> = oldEmployeeList
    private val mNewEmployeeList: List<T> = newEmployeeList
    private lateinit var itemCheckerInterface: ItemCheckerInterface<T>
    private lateinit var itemContentsCheckerInterface: ItemContentsCheckerInterface<T>

    override fun getNewListSize(): Int {
        return mOldEmployeeList.size
    }

    override fun getOldListSize(): Int {
        return mNewEmployeeList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldItemPosition >= mOldEmployeeList.size || newItemPosition >= mNewEmployeeList.size)
            return false
        return if (::itemCheckerInterface.isInitialized)
            itemCheckerInterface.isItemSame(mOldEmployeeList[oldItemPosition], mNewEmployeeList[newItemPosition])
        else
            mOldEmployeeList[oldItemPosition] === mNewEmployeeList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldItemPosition >= mOldEmployeeList.size || newItemPosition >= mNewEmployeeList.size)
            return false
        return if (::itemContentsCheckerInterface.isInitialized)
            itemContentsCheckerInterface.isItemContentsSame(mOldEmployeeList[oldItemPosition]
                    , mNewEmployeeList[newItemPosition])
        else if (::itemCheckerInterface.isInitialized)
            itemCheckerInterface.isItemSame(mOldEmployeeList[oldItemPosition], mNewEmployeeList[newItemPosition])
        else
            mOldEmployeeList[oldItemPosition] === mNewEmployeeList[newItemPosition]
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }

    fun setItemCheckerInterface(itemCheckerInterface: ItemCheckerInterface<T>) {
        this.itemCheckerInterface = itemCheckerInterface
    }

    fun setItemContentsCheckerInterface(itemContentsCheckerInterface: ItemContentsCheckerInterface<T>) {
        this.itemContentsCheckerInterface = itemContentsCheckerInterface
    }

    interface ItemCheckerInterface<T> {
        fun isItemSame(oldItem: T, newItem: T): Boolean
    }

    interface ItemContentsCheckerInterface<T> {
        fun isItemContentsSame(oldItem: T, newItem: T): Boolean
    }
}