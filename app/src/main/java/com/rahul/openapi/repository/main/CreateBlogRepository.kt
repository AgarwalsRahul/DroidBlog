package com.rahul.openapi.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import com.rahul.openapi.api.main.OpenApiMainService
import com.rahul.openapi.api.main.network_responses.BlogCreateUpdateResponse
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.models.BlogPost
import com.rahul.openapi.persistence.BlogPostDao
import com.rahul.openapi.repository.JobManager
import com.rahul.openapi.repository.NetworkBoundResource
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.ResponseType
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.rahul.openapi.util.AbsentLiveData
import com.rahul.openapi.util.DateUtils
import com.rahul.openapi.util.GenericApiResponse
import com.rahul.openapi.util.SuccessHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogRepository @Inject constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager,
) : JobManager("CreateBlogRepository") {

    private val TAG = "AppDebug"


    fun createBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateBlogViewState>> {
        return object :
            NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
                sessionManager.checkNetworkConnection(),
                true,
                false,
                shouldCancelIfNoInternet = true
            ) {
            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<BlogCreateUpdateResponse>) {
                Log.d(TAG, "createBlogPost: ${response.body.date_updated}")
                if (response.body.errorMessage != SuccessHandling.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER) {
                    val blogPost = BlogPost(
                        response.body.pk,
                        response.body.title,
                        response.body.slug,
                        response.body.body,
                        response.body.image,
                        DateUtils.convertServerStringDateToLong(response.body.date_updated),
                        response.body.username,
                    )
                    updateLocalDb(blogPost)
                }
                withContext(Dispatchers.Main) {
                    onErrorReturn(
                        response.body.errorMessage, ResponseType.Dialog()
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return openApiMainService.createBlog(
                    "Token ${authToken.token}",
                    title,
                    body,
                    image
                )
            }

            override fun loadFromCache(): LiveData<CreateBlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let {
                    blogPostDao.insert(it)
                }
            }

            override suspend fun createCacheRequestAndReturn() {
                // Not applicable in this case
            }

            override fun setJob(job: Job) {
                addJob("createBlogPost", job)
            }
        }.asLiveData()
    }
}