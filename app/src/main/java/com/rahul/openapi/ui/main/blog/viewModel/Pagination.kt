package com.rahul.openapi.ui.main.blog.viewModel

import android.util.Log
import com.rahul.openapi.ui.main.blog.state.BlogStateEvent.*
import com.rahul.openapi.ui.main.blog.state.BlogViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import setBlogListData
import setQueryExhausted


@FlowPreview
@ExperimentalCoroutinesApi
fun BlogViewModel.resetPage(){
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

@FlowPreview
@ExperimentalCoroutinesApi
fun BlogViewModel.refreshFromCache(){
    setQueryExhausted(false)
    setStateEvent(RestoreBlogListFromCache())
}

@FlowPreview
@ExperimentalCoroutinesApi
fun BlogViewModel.loadFirstPage() {
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogSearchEvent())
    Log.e(TAG, "BlogViewModel: loadFirstPage: ${viewState.value!!.blogFields.searchQuery}")
}

@FlowPreview
@ExperimentalCoroutinesApi
private fun BlogViewModel.incrementPageNumber(){
    val update = getCurrentViewStateOrNew()
    val page = update.copy().blogFields.page // get current page
    update.blogFields.page = page?.plus(1)
    setViewState(update)
}

@FlowPreview
@ExperimentalCoroutinesApi
fun BlogViewModel.nextPage(){
    if(!isJobAlreadyActive(BlogSearchEvent())
        && !viewState.value!!.blogFields.isQueryExhausted!!
    ){
        Log.d(TAG, "BlogViewModel: Attempting to load next page...")
        incrementPageNumber()
        setStateEvent(BlogSearchEvent())
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
fun BlogViewModel.handleIncomingBlogListData(viewState: BlogViewState){
    viewState.blogFields.let { blogFields ->
        blogFields.blogList?.let { setBlogListData(it) }
        blogFields.isQueryExhausted?.let {  setQueryExhausted(it) }
    }
}