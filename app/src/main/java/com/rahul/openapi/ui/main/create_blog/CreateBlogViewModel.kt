package com.rahul.openapi.ui.main.create_blog

import android.net.Uri
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.repository.main.CreateBlogRepositoryImpl
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.BaseViewModel
import com.rahul.openapi.ui.*
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogStateEvent.*
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogViewState.*
import com.rahul.openapi.util.*
import com.rahul.openapi.util.ErrorHandling.Companion.INVALID_STATE_EVENT

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
@MainScope
class CreateBlogViewModel
@Inject
constructor(
    val createBlogRepository: CreateBlogRepositoryImpl,
    val sessionManager: SessionManager
): BaseViewModel<CreateBlogViewState>() {


    override fun handleNewData(stateEvent: StateEvent?, data: CreateBlogViewState) {

        setNewBlogFields(
            data.newBlogFields.newBlogTitle,
            data.newBlogFields.newBlogBody,
            data.newBlogFields.newBlogImage
        )
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedToken.value?.let { authToken ->
            val job: Flow<DataState<CreateBlogViewState>> = when(stateEvent){

                is CreateNewBlogEvent -> {
                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    createBlogRepository.createNewBlogPost(
                        stateEvent = stateEvent,
                        authToken = authToken,
                        title = title,
                        body = body,
                        image = stateEvent.image
                    )
                }

                else -> {
                    flow{
                        emit(
                            DataState.error<CreateBlogViewState>(
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

    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?){
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.newBlogFields
        title?.let{ newBlogFields.newBlogTitle = it }
        body?.let{ newBlogFields.newBlogBody = it }
        uri?.let{ newBlogFields.newBlogImage = it }
        update.newBlogFields = newBlogFields
        _viewState.value = update
    }

    fun clearNewBlogFields(){
        val update = getCurrentViewStateOrNew()
        update.newBlogFields = NewBlogFields()
        setViewState(update)
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}