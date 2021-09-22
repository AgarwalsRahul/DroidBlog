package com.rahul.openapi.ui.main.blog.viewModel

import android.content.SharedPreferences
import clearLayoutManagerState
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.persistence.BlogQueryUtils
import com.rahul.openapi.repository.main.BlogRepositoryImpl
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.*
import com.rahul.openapi.ui.main.blog.state.BlogStateEvent.*
import com.rahul.openapi.ui.main.blog.state.BlogViewState
import com.rahul.openapi.util.*
import com.rahul.openapi.util.ErrorHandling.Companion.INVALID_STATE_EVENT
import com.rahul.openapi.util.PreferenceKeys.Companion.BLOG_FILTER
import com.rahul.openapi.util.PreferenceKeys.Companion.BLOG_ORDER


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.RequestBody
import setBlogFilter
import setBlogOrder
import setBlogPost
import setIsAuthorOfBlogPost
import setQueryExhausted
import setUpdatedBody
import setUpdatedTitle
import setUpdatedUri
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@MainScope
class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepositoryImpl,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
): BaseViewModel<BlogViewState>(){

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )
        setBlogOrder(
            sharedPreferences.getString(
                BLOG_ORDER,
                BlogQueryUtils.BLOG_ORDER_ASC
            )
        )
    }

    override fun handleNewData(stateEvent: StateEvent?, data: BlogViewState) {

        data.blogFields.let { blogFields ->

            blogFields.blogList?.let { blogList ->
                handleIncomingBlogListData(data)
            }

            blogFields.isQueryExhausted?.let { isQueryExhausted ->
                setQueryExhausted(isQueryExhausted)
            }
        }

        data.viewBlogFields.let { viewBlogFields ->

            viewBlogFields.blogPost?.let { blogPost ->
                setBlogPost(blogPost)
            }

            viewBlogFields.isAuthorOfPost?.let { isAuthor ->
                setIsAuthorOfBlogPost(isAuthor)
            }
        }

        data.updatedBlogFields.let { updatedBlogFields ->

            updatedBlogFields.updatedImageUri?.let { uri ->
                setUpdatedUri(uri)
            }

            updatedBlogFields.updatedBlogTitle?.let { title ->
                setUpdatedTitle(title)
            }

            updatedBlogFields.updatedBlogBody?.let { body ->
                setUpdatedBody(body)
            }
        }

        _activeStateEventTracker.removeStateEvent(stateEvent)
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedToken.value?.let { authToken ->
            val job: Flow<DataState<BlogViewState>> = when(stateEvent){

                is BlogSearchEvent -> {
                    clearLayoutManagerState()
                    blogRepository.searchBlogPosts(
                        stateEvent = stateEvent,
                        authToken = authToken,
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage()
                    )
                }

                is RestoreBlogListFromCache -> {
                    blogRepository.restoreBlogListFromCache(
                        stateEvent = stateEvent,
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage()
                    )
                }

                is CheckAuthorOfBlogPost -> {
                    blogRepository.isAuthorOfBlogPost(
                        stateEvent = stateEvent,
                        authToken = authToken,
                        slug = getSlug()
                    )
                }

                is DeleteBlogPostEvent -> {
                    blogRepository.deleteBlogPost(
                        stateEvent = stateEvent,
                        authToken = authToken,
                        blogPost = getBlogPost()
                    )
                }

                is UpdateBlogPostEvent -> {
                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    blogRepository.updateBlogPost(
                        stateEvent = stateEvent,
                        authToken = authToken,
                        slug = getSlug(),
                        title = title,
                        body = body,
                        image = stateEvent.image
                    )
                }

                else -> {
                    flow{
                        emit(
                            DataState.error<BlogViewState>(
                                response = Response(
                                    message = INVALID_STATE_EVENT,
                                    uiComponentType = UIComponentType.None(),
                                    messageType = MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        )
                    }
                }
            }
            launchJob(stateEvent, job)
        }?: sessionManager.logout()

    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun saveFilterOptions(filter: String, order: String){
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
