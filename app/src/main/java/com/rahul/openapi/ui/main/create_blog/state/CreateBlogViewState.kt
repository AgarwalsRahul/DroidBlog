package com.rahul.openapi.ui.main.create_blog.state

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

const val CREATE_BLOG_VIEW_STATE_BUNDLE_KEY = "CreateBlogViewState"


@Parcelize
data class CreateBlogViewState(
    var newBlogFields: NewBlogFields = NewBlogFields()
) : Parcelable {

    @Parcelize
    data class NewBlogFields(
        var newBlogTitle: String? = null,
        var newBlogBody: String? = null,
        var newBlogImage: Uri? = null,
    ) : Parcelable
}