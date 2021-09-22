package com.rahul.openapi.repository.main


import com.rahul.openapi.repository.safeApiCall
import com.rahul.openapi.util.*
import com.rahul.openapi.api.main.OpenApiMainService
import com.rahul.openapi.api.main.network_responses.BlogCreateUpdateResponse
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.persistence.BlogPostDao
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.rahul.openapi.util.DataState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import com.rahul.openapi.ui.*
import com.rahul.openapi.util.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER

@FlowPreview
@MainScope
class CreateBlogRepositoryImpl
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): CreateBlogRepository {

    private val TAG: String = "AppDebug"

    override fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow{

        val apiResult = safeApiCall(IO){
            openApiMainService.createBlog(
                "Token ${authToken.token!!}",
                title,
                body,
                image
            )
        }

        emit(
            object: ApiResponseHandler<CreateBlogViewState, BlogCreateUpdateResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<CreateBlogViewState> {

                    // If they don't have a paid membership account it will still return a 200
                    // Need to account for that
                    if (!resultObj.response.equals(RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER)) {
                        val updatedBlogPost = resultObj.toBlogPost()
                        blogPostDao.insert(updatedBlogPost)
                    }
                    return DataState.data(
                        response = Response(
                            message = resultObj.response,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

}
