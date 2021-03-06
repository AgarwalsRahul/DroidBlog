package com.rahul.openapi.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {

    companion object {
        private const val TAG = "AppDebug"
    }

    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    val viewState: LiveData<ViewState>
        get() = _viewState

    val dataState: LiveData<DataState<ViewState>> = Transformations.switchMap(
        _stateEvent
    ) { stateEvent ->
        stateEvent?.let {
            handleStateEvent(it)
        }
    }

    fun setStateEvent(event: StateEvent) {
        _stateEvent.value = event
    }

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    fun getCurrentViewStateOrNew(): ViewState {
        val value = viewState.value?.let {
            it
        } ?: initNewViewState()
        return value
    }

    abstract fun initNewViewState(): ViewState

    abstract fun handleStateEvent(it: StateEvent): LiveData<DataState<ViewState>>


}