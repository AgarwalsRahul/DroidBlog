package com.rahul.openapi.ui.auth

import androidx.lifecycle.LiveData
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.repository.auth.AuthRepository
import com.rahul.openapi.ui.BaseViewModel
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.auth.state.AuthStateEvent
import com.rahul.openapi.ui.auth.state.AuthStateEvent.*
import com.rahul.openapi.ui.auth.state.AuthViewState
import com.rahul.openapi.ui.auth.state.LoginFields
import com.rahul.openapi.ui.auth.state.RegistrationFields
import javax.inject.Inject

class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) :
    BaseViewModel<AuthStateEvent, AuthViewState>() {

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        _viewState.value = update
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        _viewState.value = update
    }


    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    override fun handleStateEvent(it: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        return when (it) {
            is LoginEvent -> {
                authRepository.attemptLogin(it.email, it.password)
            }
            is RegisterEvent -> {
                authRepository.attemptRegistration(
                    it.email,
                    it.username,
                    it.password,
                    it.confirm_password
                )
            }
            is checkPreviousAuthEvent -> {
                authRepository.checkPreviousAuthUser()
            }
            is None -> object:LiveData<DataState<AuthViewState>>(){
                override fun onActive() {
                    super.onActive()
                    value = DataState.data(null,null)
                }
            }
        }
    }

    private fun handlePendingData(){
        setStateEvent(AuthStateEvent.None())
    }

    fun cancelActiveJobs() {
        handlePendingData()
        authRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}