package com.rahul.openapi.repository.main
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.rahul.openapi.util.DataState
import com.rahul.openapi.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

@FlowPreview
@MainScope
interface CreateBlogRepository {

    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ): Flow<DataState<CreateBlogViewState>>
}