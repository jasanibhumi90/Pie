package com.pie.utils

import android.view.View
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wang.avi.AVLoadingIndicatorView


class Pagination(
    private val recyclerView: RecyclerView, private val onLoadMore: OnLoadMore,
    private var loadMore: Boolean = true
) {
    companion object {
        private const val TAG = "Pagination"
        private const val VISIBLE_THRESHOLD: Int = 2
    }

    var layoutManager: RecyclerView.LayoutManager = recyclerView.layoutManager!!

    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var firstVisibleItem: Int = 0
    private var visibleItemPosition: Int = 0
    private var maxCalled: Int = -1

    private var loading: Boolean = false
    private var pageIndex: Int = 1
    private var progressView: View? = null


    private val scrollEnd: MutableLiveData<Boolean> = MutableLiveData()

    var lastScrolledPos: Int = 0

    private lateinit var onWatchItem: OnWatchItem

    fun getLastVisibleItem(): Int {
        if (layoutManager is LinearLayoutManager) {
            return (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        }
        if (layoutManager is GridLayoutManager) {
            return (layoutManager as GridLayoutManager).findLastVisibleItemPosition()
        }
        return RecyclerView.NO_POSITION
    }

    fun getLastCompletelyVisibleItem(): Int {
        if (layoutManager is LinearLayoutManager) {
            return (layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
        }
        if (layoutManager is GridLayoutManager) {
            return (layoutManager as GridLayoutManager).findLastCompletelyVisibleItemPosition()
        }
        return RecyclerView.NO_POSITION
    }

    fun getFirstVisibleItem(): Int {
        if (layoutManager is LinearLayoutManager) {
            return (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }
        if (layoutManager is GridLayoutManager) {
            return (layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
        }
        return RecyclerView.NO_POSITION
    }

    fun getItemCount(): Int {
        return layoutManager.itemCount
    }

    /**
     * start pagination
     */
    fun setPaginationListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy != 0) {
                    totalItemCount = getItemCount()
                    lastVisibleItem = getLastVisibleItem()
                    if (!loading && loadMore && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                        loading = true
                        showProgress()
                        onLoadMore.onLoadMore()
                        AppLogger.e(TAG, "onScrolled page => $pageIndex")
                    }
                }
            }
        })
    }

    fun loadMore() {
        if (!loading && loadMore) {
            loading = true
            showProgress()
            onLoadMore.onLoadMore()
            AppLogger.e(TAG, "onScrolled page => $pageIndex")
        }
    }

    /**
     * start pagination
     */
    fun setUpSidePaginationListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy != 0) {
                    if (!loading && loadMore && getFirstVisibleItem() <= VISIBLE_THRESHOLD) {
                        loading = true
                        showProgress()
                        onLoadMore.onLoadMore()
                        AppLogger.e(TAG, "onScrolled page => $pageIndex")
                    }
                }

            }
        })
    }

    /**
     * @param progressView view for show load more progress bar
     *                     can attach avi loader it will handle by smoothToHide and smoothToShow
     *                     <br/>for progressbar setIndeterminate value will be set to true
     *                     <br/>and for other view just visibility will maintain
     */
    public fun setProgressView(progressView: View) {
        this.progressView = progressView
        if (progressView is ProgressBar) {
            progressView.isIndeterminate = true
        }
    }


    /**
     * start pagination with view item listener
     */
    fun setPaginationListenerWithItemWatcher(lifecycleOwner: LifecycleOwner) {
        scrollEnd.value = false

        scrollEnd.observe(lifecycleOwner, Observer {
            it?.let { it1 ->
                if (it1) {
                    var scrollEndToPos = getLastCompletelyVisibleItem()
                    if (::onWatchItem.isInitialized) {
                        //todo check condition
                        if (scrollEndToPos == -1) {
                            scrollEndToPos =
                                if (getItemCount() == getLastVisibleItem())
                                    getLastVisibleItem()
                                else getLastVisibleItem() - 1
                        }
                        if (lastScrolledPos < scrollEndToPos) {
                            onWatchItem.onWatchItem(lastScrolledPos, scrollEndToPos)
                            lastScrolledPos = scrollEndToPos
                        }
                    }
                }
            }
        })


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                scrollEnd.postValue(newState == SCROLL_STATE_IDLE)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy != 0) {
                    totalItemCount = getItemCount()
                    lastVisibleItem = getLastVisibleItem()
                    if (!loading && loadMore && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                        onLoadMore.onLoadMore()
                        loading = true
                        showProgress()
                        AppLogger.e(TAG, "onScrolled page => $pageIndex")
                    }
                }
            }
        })
    }

    /**
     * view item listener
     */
    fun setItemWatcher(lifecycleOwner: LifecycleOwner) {
        scrollEnd.value = false

        scrollEnd.observe(lifecycleOwner, Observer {
            it?.let { it1 ->
                if (it1) {
                    val scrollEndToPos = getLastCompletelyVisibleItem()
                    if (::onWatchItem.isInitialized) {
                        //todo check condition
                        if (lastScrolledPos < scrollEndToPos)
                            onWatchItem.onWatchItem(lastScrolledPos, scrollEndToPos)
                        lastScrolledPos = scrollEndToPos
                    }
                }
            }
        })
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                scrollEnd.postValue(newState == SCROLL_STATE_IDLE)
            }
        })
    }

    private fun showProgress() {
        if (progressView is AVLoadingIndicatorView) {
            (progressView as AVLoadingIndicatorView).smoothToShow()
        } else {
            progressView?.visibility = View.VISIBLE
        }
    }

    private fun hideProgress() {
        if (progressView is AVLoadingIndicatorView) {
            (progressView as AVLoadingIndicatorView).smoothToHide()
        } else {
            progressView?.visibility = View.GONE
        }
    }

    /**
     * @param loadMore do you want load more item
     */
    fun setLoadMore(loadMore: Boolean) {
        this.loadMore = loadMore
    }

    fun setItemLoaded() {
        loading = false
        hideProgress()
    }

    interface OnLoadMore {
        fun onLoadMore()
    }

    fun setOnWatchItem(onWatchItem: OnWatchItem) {
        this.onWatchItem = onWatchItem
    }

    interface OnWatchItem {
        fun onWatchItem(startPos: Int, endPos: Int)
    }
}