package com.rahul.openapi.repository.main
import android.util.Log
import com.rahul.openapi.api.GenericResponse
import com.rahul.openapi.api.main.OpenApiMainService
import com.rahul.openapi.api.main.network_responses.BlogCreateUpdateResponse
import com.rahul.openapi.api.main.network_responses.BlogListSearchResponse
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.models.BlogPost
import com.rahul.openapi.persistence.BlogPostDao
import com.rahul.openapi.persistence.returnOrderedBlogQuery
import com.rahul.openapi.repository.NetworkBoundResource
import com.rahul.openapi.repository.buildError
import com.rahul.openapi.repository.safeApiCall
import com.rahul.openapi.repository.safeCacheCall
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.*
import com.rahul.openapi.ui.UIComponentType
import com.rahul.openapi.ui.main.blog.state.BlogViewState
import com.rahul.openapi.ui.main.blog.state.BlogViewState.*
import com.rahul.openapi.util.*
import com.rahul.openapi.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.rahul.openapi.util.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.rahul.openapi.util.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import com.rahul.openapi.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@FlowPreview
@MainScope
class BlogRepositoryImpl
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): BlogRepository
{

    private val TAG: String = "AppDebug"
    override fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>> {
        return object: NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            dispatcher = IO,
            stateEvent = stateEvent,
            apiCall = {
                openApiMainService.searchListBlogPosts(
                    "Token ${authToken.token!!}",
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            },
            cacheCall = {
                blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
            }
        ){
            override suspend fun updateCache(networkObject: BlogListSearchResponse) {
                val blogPostList = networkObject.toList()
                withContext(IO) {
                    for(blogPost in blogPostList){
                        try{
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(TAG, "updateLocalDb: inserting blog: ${blogPost}")
                                blogPostDao.insert(blogPost)
                            }
                        }catch (e: Exception){
                            Log.e(TAG, "updateLocalDb: error updating cache data on blog post with slug: ${blogPost.slug}. " +
                                    "${e.message}")
                            // Could send an error report here or something but I don't think you should throw an error to the UI
                            // Since there could be many blog posts being inserted/updated.
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<BlogPost>): DataState<BlogViewState> {
                val viewState = BlogViewState(
                    blogFields = BlogFields(
                        blogList = resultObj
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun restoreBlogListFromCache(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ) = flow{

        val cacheResult = safeCacheCall(IO){
            blogPostDao.returnOrderedBlogQuery(
                query = query,
                filterAndOrder = filterAndOrder,
                page = page)
        }
        emit(
            object: CacheResponseHandler<BlogViewState, List<BlogPost>>(
                response = cacheResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(
                    resultObj: List<BlogPost>
                ): DataState<BlogViewState> {
                    val viewState = BlogViewState(
                        blogFields = BlogFields(
                            blogList = resultObj
                        )
                    )
                    return DataState.data(
                        response = null,
                        data = viewState,
                        stateEvent = stateEvent
                    )
                }

            }.getResult()
        )
    }

    override fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO){
            openApiMainService.isAuthorOfBlogPost(
                "Token ${authToken.token!!}",
                slug
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: GenericResponse): DataState<BlogViewState> {
                    val viewState = BlogViewState(
                        viewBlogFields = ViewBlogFields(
                            isAuthorOfPost = false
                        )
                    )
                    return when {

                        resultObj.response.equals(RESPONSE_NO_PERMISSION_TO_EDIT) -> {
                            DataState.data(
                                response = null,
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        }

                        resultObj.response.equals(RESPONSE_HAS_PERMISSION_TO_EDIT) -> {
                            viewState.viewBlogFields.isAuthorOfPost = true
                            DataState.data(
                                response = null,
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        }

                        else -> {
                            buildError(
                                ERROR_UNKNOWN,
                                UIComponentType.None(),
                                stateEvent
                            )
                        }
                    }
                }
            }.getResult()
        )
    }

    override fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost,
        stateEvent: StateEvent
    ) =  flow {
        val apiResult = safeApiCall(IO){
            openApiMainService.deleteBlogPost(
                "Token ${authToken.token!!}",
                blogPost.slug
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: GenericResponse): DataState<BlogViewState> {

                    if(resultObj.response == SUCCESS_BLOG_DELETED){
                        blogPostDao.deleteBlogPost(blogPost)
                        return DataState.data(
                            response = Response(
                                message = SUCCESS_BLOG_DELETED,
                                uiComponentType = UIComponentType.Toast(),
                                messageType = MessageType.Success()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                    else{
                        return buildError(
                            ERROR_UNKNOWN,
                            UIComponentType.Dialog(),
                            stateEvent
                        )
                    }
                }
            }.getResult()
        )
    }

    override fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow{

        val apiResult = safeApiCall(IO){
            openApiMainService.updateBlog(
                "Token ${authToken.token!!}",
                slug,
                title,
                body,
                image
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, BlogCreateUpdateResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<BlogViewState> {

                    val updatedBlogPost = resultObj.toBlogPost()

                    blogPostDao.updateBlogPost(
                        updatedBlogPost.pk,
                        updatedBlogPost.title,
                        updatedBlogPost.body,
                        updatedBlogPost.image
                    )

                    return DataState.data(
                        response = Response(
                            message = resultObj.response,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data =  BlogViewState(
                            viewBlogFields = ViewBlogFields(
                                blogPost = updatedBlogPost
                            )
                        ),
                        stateEvent = stateEvent
                    )

                }

            }.getResult()
        )
    }


}