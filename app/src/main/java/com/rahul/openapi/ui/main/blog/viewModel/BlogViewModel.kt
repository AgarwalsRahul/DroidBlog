package com.rahul.openapi.ui.main.blog.viewModel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.rahul.openapi.persistence.BlogQueryUtils
import com.rahul.openapi.repository.main.BlogRepository
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.BaseViewModel
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.Loading
import com.rahul.openapi.ui.main.blog.state.BlogStateEvent
import com.rahul.openapi.ui.main.blog.state.BlogViewState
import com.rahul.openapi.util.AbsentLiveData
import com.rahul.openapi.util.PreferenceKeys.Companion.BLOG_FILTER
import com.rahul.openapi.util.PreferenceKeys.Companion.BLOG_ORDER
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) : BaseViewModel<BlogStateEvent, BlogViewState>() {

    init {
        setFilter(sharedPreferences.getString(BLOG_FILTER, BlogQueryUtils.BLOG_FILTER_DATE_UPDATED))

        setOrder(sharedPreferences.getString(BLOG_ORDER, BlogQueryUtils.BLOG_ORDER_ASC)!!)
    }

    val TAG = "BlogViewModel"
    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        return when (stateEvent) {

            is BlogStateEvent.BlogSearchEvent -> {
                clearLayoutManagerState()
                sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken,
                        getSearchQuery(),
                        getOrder() + getFilter(),
                        getPage()
                    )
                } ?: AbsentLiveData.create()
            }
            is BlogStateEvent.RestoreBlogList -> {
                blogRepository.restoreBlogListFromCache(
                    getSearchQuery(),
                    getOrder() + getFilter(),
                    getPage()
                )
            }
            is BlogStateEvent.CheckAuthorOfBlogPost -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.isAuthorOfBlogPost(
                        authToken,
                        getSlug()
                    )
                } ?: AbsentLiveData.create()
            }

            is BlogStateEvent.DeleteBlogPostEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.deleteBlogPost(
                        authToken,
                        getBlogPost()
                    )
                } ?: AbsentLiveData.create()
            }

            is BlogStateEvent.UpdatedBlogPostEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.updateBlogPost(
                        authToken,
                        getSlug(),
                        title = RequestBody.create(MediaType.parse("text/plain"), stateEvent.title),
                        body = RequestBody.create(MediaType.parse("text/plain"), stateEvent.body),
                        stateEvent.image
                    )
                } ?: AbsentLiveData.create()
            }

            is BlogStateEvent.None -> {
                object : LiveData<DataState<BlogViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }


    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }


    fun cancelActiveJobs() {
        blogRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    private fun handlePendingData() {
        setStateEvent(BlogStateEvent.None())
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.apply()
        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}