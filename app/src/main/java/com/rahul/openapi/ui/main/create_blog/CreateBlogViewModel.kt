package com.rahul.openapi.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.LiveData
import com.rahul.openapi.repository.main.CreateBlogRepository
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.BaseViewModel
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.Loading
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogStateEvent
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.rahul.openapi.util.AbsentLiveData
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val createBlogRepository: CreateBlogRepository,
) : BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {
    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    override fun handleStateEvent(it: CreateBlogStateEvent): LiveData<DataState<CreateBlogViewState>> {
        return when (it) {
            is CreateBlogStateEvent.CreateNewBlogEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    createBlogRepository.createBlogPost(
                        authToken,
                        title = RequestBody.create(MediaType.parse("text/plain"), it.title),
                        body = RequestBody.create(MediaType.parse("text/plain"), it.body),
                        it.image,
                    )
                } ?: AbsentLiveData.create()
            }
            is CreateBlogStateEvent.None -> {
                object : LiveData<DataState<CreateBlogViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }

            }
        }
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.newBlogFields
        title?.let { newBlogFields.newBlogTitle = it }
        body?.let { newBlogFields.newBlogBody = it }
        uri?.let { newBlogFields.newBlogImage = it }
        update.newBlogFields = newBlogFields
        _viewState.value = update
    }

    fun getNewImageUri():Uri?{
        getCurrentViewStateOrNew().let {viewState->
            viewState.newBlogFields.let {
                return it.newBlogImage
            }
        }
    }

    fun clearNewBlogFields() {
        val update = getCurrentViewStateOrNew()
        update.newBlogFields = CreateBlogViewState.NewBlogFields()
        setViewState(update)
    }

    fun cancelActiveJobs() {
        createBlogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(CreateBlogStateEvent.None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}