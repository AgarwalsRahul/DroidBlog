package com.rahul.openapi.ui.main.blog.viewModel

import android.util.Log
import com.rahul.openapi.ui.main.blog.state.BlogStateEvent.*
import com.rahul.openapi.ui.main.blog.state.BlogViewState

fun BlogViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

fun BlogViewModel.restoreFromCache(){
    setQueryInProgress(true)
    setQueryExhausted(false)
    setStateEvent(RestoreBlogList())
}

fun BlogViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogSearchEvent())
}

fun BlogViewModel.incrementPage() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().blogFields.page
    update.blogFields.page = page + 1
    setViewState(update)
}


fun BlogViewModel.nextPage() {
    if (!getIsQueryExhausted() || !getIsQueryInProgress()) {
        Log.d(TAG, "BlogViewModel: Attempting to load next page...")
        incrementPage()
        setQueryInProgress(true)
        setStateEvent(BlogSearchEvent())
    }
}

fun BlogViewModel.handleIncomingBlogListData(viewState:BlogViewState){
    setQueryExhausted(viewState.blogFields.isQueryExhausted)
    setQueryInProgress(viewState.blogFields.isQueryInProgress)
    setBlogListData(viewState.blogFields.blogList)
}