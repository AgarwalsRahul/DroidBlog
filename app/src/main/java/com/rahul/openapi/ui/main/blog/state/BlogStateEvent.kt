package com.rahul.openapi.ui.main.blog.state

import okhttp3.MultipartBody

sealed class BlogStateEvent {
    class BlogSearchEvent() : BlogStateEvent()

    class None : BlogStateEvent()

    class DeleteBlogPostEvent : BlogStateEvent()

    class CheckAuthorOfBlogPost : BlogStateEvent()

    class RestoreBlogList():BlogStateEvent()

    data class UpdatedBlogPostEvent(
        var title: String?,
        var body: String,
        var image: MultipartBody.Part?
    ) : BlogStateEvent()
}