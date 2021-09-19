package com.rahul.openapi.ui.main.account

import androidx.lifecycle.LiveData
import com.rahul.openapi.models.AccountProperties
import com.rahul.openapi.repository.main.AccountRepository
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.BaseViewModel
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.main.account.state.AccountStateEvent
import com.rahul.openapi.ui.main.account.state.AccountStateEvent.*
import com.rahul.openapi.ui.main.account.state.AccountViewState
import com.rahul.openapi.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<AccountStateEvent, AccountViewState>() {


    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    override fun handleStateEvent(it: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        return when (it) {
            is GetAccountPropertiesEvent -> {
                sessionManager.cachedToken.value?.let {
                    accountRepository.getAccountProperties(it)
                } ?: AbsentLiveData.create<DataState<AccountViewState>>()
            }
            is ChangePasswordEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.changePassword(
                        authToken,
                        it.currentPassword,
                        it.newPassword,
                        it.confirmNewPassword
                    )
                } ?: AbsentLiveData.create<DataState<AccountViewState>>()
            }
            is UpdateAccountPropertiesEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { pk ->
                        accountRepository.updateAccountProperties(
                            authToken, AccountProperties(
                                pk, it.email, it.username
                            )
                        )
                    }
                } ?: AbsentLiveData.create<DataState<AccountViewState>>()
            }
            is None -> {
                AbsentLiveData.create<DataState<AccountViewState>>()
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    fun logOut() {
        sessionManager.logout()
    }

    private fun handlePendingData() {
        setStateEvent(AccountStateEvent.None())
    }

    fun cancelActiveJobs() {
        handlePendingData()
        accountRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}