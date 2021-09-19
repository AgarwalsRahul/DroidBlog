package com.rahul.openapi.ui.main.blog.viewModel

import android.net.Uri
import com.rahul.openapi.models.BlogPost

fun BlogViewModel.getPage(): Int {
    getCurrentViewStateOrNew().let {
        return it.blogFields.page
    }
}

fun BlogViewModel.getIsQueryExhausted(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryExhausted
    }
}

fun BlogViewModel.getIsQueryInProgress(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryInProgress
    }
}

fun BlogViewModel.getSearchQuery(): String {
    getCurrentViewStateOrNew().let {
        return it.blogFields.searchQuery
    }
}

fun BlogViewModel.getFilter(): String {
    getCurrentViewStateOrNew().let {
        return it.blogFields.filter
    }
}


fun BlogViewModel.getOrder(): String {
    getCurrentViewStateOrNew().let {
        return it.blogFields.order
    }
}


fun BlogViewModel.getSlug(): String {
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.blogPost?.let { blogPost ->
            blogPost.slug
        } ?: ""
    }
}

fun BlogViewModel.getIsAuthorOfBlogPost(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.isAuthorOfPost
    }
}

fun BlogViewModel.getBlogPost(): BlogPost {
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.blogPost?.let {
            return it
        }?: getDummyBlogPost()
    }
}

fun BlogViewModel.getDummyBlogPost(): BlogPost{
    return BlogPost(-1, "" , "", "", "", 1, "")
}


fun BlogViewModel.getUpdatedBlogUri(): Uri? {
    getCurrentViewStateOrNew().let {
        it.updateBlogFields.updatedBlogURI?.let {
            return it
        }
    }
    return null
}