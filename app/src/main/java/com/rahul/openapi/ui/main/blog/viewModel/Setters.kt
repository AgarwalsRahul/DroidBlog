package com.rahul.openapi.ui.main.blog.viewModel

import android.net.Uri
import android.os.Parcelable
import com.rahul.openapi.models.BlogPost


fun BlogViewModel.setQuery(query: String) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.searchQuery = query
    setViewState(update)
}

fun BlogViewModel.setBlogListData(blogList: List<BlogPost>) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.blogList = blogList
    setViewState(update)
}

fun BlogViewModel.setBlogPost(blogPost: BlogPost){
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.blogPost=blogPost
    setViewState(update)
}

fun BlogViewModel.setQueryExhausted(isQueryExhausted:Boolean){
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryExhausted=isQueryExhausted
    setViewState(update)
}

fun BlogViewModel.setQueryInProgress(isQueryInProgress:Boolean){
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryInProgress=isQueryInProgress
    setViewState(update)
}




fun BlogViewModel.setAuthorOfBlog(isAuthorOfBlog: Boolean){
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.isAuthorOfPost=isAuthorOfBlog
    setViewState(update)
}


fun BlogViewModel.setFilter(filter: String?){
   filter?.let {
       val update = getCurrentViewStateOrNew()
       update.blogFields.filter=filter
       setViewState(update)
   }
}

fun BlogViewModel.setOrder(order: String){
    val update = getCurrentViewStateOrNew()
    update.blogFields.order=order
    setViewState(update)
}

fun BlogViewModel.removeDeletedBlogPost(){
    val update = getCurrentViewStateOrNew()
    val list =update.blogFields.blogList.toMutableList()
    list.remove(getBlogPost())
    setBlogListData(list)
}

fun BlogViewModel.setUpdatedBlogFields(title: String?, body: String?, uri: Uri?){
    val update = getCurrentViewStateOrNew()
    val updatedBlogFields = update.updateBlogFields
    title?.let{ updatedBlogFields.updatedBlogTitle = it }
    body?.let{ updatedBlogFields.updatedBlogBody = it }
    uri?.let{ updatedBlogFields.updatedBlogURI = it }
    update.updateBlogFields = updatedBlogFields
    setViewState(update)
}

fun BlogViewModel.setLayoutManagerState(lmState:Parcelable){
    val update = getCurrentViewStateOrNew()
    update.blogFields.layoutManagerState=lmState
    setViewState(update)
}

fun BlogViewModel.clearLayoutManagerState(){
    val update = getCurrentViewStateOrNew()
    update.blogFields.layoutManagerState=null
    setViewState(update)
}

fun BlogViewModel.updateListItem(newBlogPost: BlogPost){
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for(i in 0 until list.size){
        if(list[i].pk == newBlogPost.pk){
            list[i] = newBlogPost
            break
        }
    }
    update.blogFields.blogList = list
    setViewState(update)
}


fun BlogViewModel.onBlogPostUpdateSuccess(blogPost: BlogPost){
    setUpdatedBlogFields(
        uri = null,
        title = blogPost.title,
        body = blogPost.body
    ) // update UpdateBlogFragment (not really necessary since navigating back)
    setBlogPost(blogPost) // update ViewBlogFragment
    updateListItem(blogPost) // update BlogFragment
}

