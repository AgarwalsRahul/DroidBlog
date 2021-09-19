package com.rahul.openapi.ui.main.blog.state

import android.net.Uri
import android.os.Parcelable
import com.rahul.openapi.models.BlogPost
import com.rahul.openapi.persistence.BlogQueryUtils
import kotlinx.android.parcel.Parcelize


const val BLOG_VIEW_STATE_BUNDLE_KEY = "BlogViewState"

@Parcelize
data class BlogViewState(
    var blogFields: BlogFields = BlogFields(),

    var viewBlogFields: ViewBlogFields = ViewBlogFields(),
    var updateBlogFields: UpdateBlogFields = UpdateBlogFields()
) : Parcelable {
    @Parcelize
    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = BlogQueryUtils.ORDER_BY_ASC_DATE_UPDATED,
        var order: String = BlogQueryUtils.BLOG_ORDER_ASC,
        var layoutManagerState: Parcelable? = null,
    ) : Parcelable

    @Parcelize
    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfPost: Boolean = false
    ) : Parcelable

    @Parcelize
    data class UpdateBlogFields(
        var updatedBlogTitle: String? = null,
        var updatedBlogBody: String? = null,
        var updatedBlogURI: Uri? = null,
    ) : Parcelable
}