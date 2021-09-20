package com.rahul.openapi.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.rahul.openapi.api.GenericResponse
import com.rahul.openapi.api.main.OpenApiMainService
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.models.AccountProperties
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.persistence.AccountPropertiesDao
import com.rahul.openapi.repository.JobManager
import com.rahul.openapi.repository.NetworkBoundResource
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.Response
import com.rahul.openapi.ui.ResponseType
import com.rahul.openapi.ui.main.account.state.AccountViewState
import com.rahul.openapi.util.AbsentLiveData
import com.rahul.openapi.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

@MainScope
class AccountRepository @Inject constructor(
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiMainService: OpenApiMainService,
    val sessionManager: SessionManager,
) : JobManager("AccountRepository"){

    private val TAG = "AppDebug"



    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.checkNetworkConnection(),
                true, true, false
            ) {
            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return openApiMainService.getAccountProperties("Token ${authToken.token}")
            }

            //
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    // finish by viewing the db cache
                    result.addSource(loadFromCache()) {
                        onCompleteJob(DataState.data(data = it, null))

                    }
                }
            }

            override fun setJob(job: Job) {
                addJob("getAccountProperties",job)
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                val source = accountPropertiesDao.searchByPk(authToken.account_pk!!)
                return Transformations.switchMap(source) {
                    object : LiveData<AccountViewState>() {
                        override fun onActive() {
                            super.onActive()
                            value = AccountViewState(it)
                        }
                    }
                }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        it.pk,
                        it.email,
                        it.username
                    )
                }
            }

        }.asLiveData()
    }

    fun updateAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.checkNetworkConnection(), true, false, true
        ) {
            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null)

                onCompleteJob(
                    DataState.data(
                        null, Response(
                            response.body.response, responseType = ResponseType.Toast()
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.updateAccountProperties(
                    "Token ${authToken.token!!}",
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                // Not use in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override suspend fun createCacheRequestAndReturn() {
                // Not used in this case
            }

            override fun setJob(job: Job) {
                addJob("updateAccountProperties",job)
            }

        }.asLiveData()
    }

    fun changePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.checkNetworkConnection(),
            true,
            false,
            true
        ) {
            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<GenericResponse>) {
                onCompleteJob(
                    DataState.data(
                        null,
                        Response(response.body.response, ResponseType.Toast())
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.changePassword(
                    "Token ${authToken.token!!}",
                    currentPassword, newPassword, confirmNewPassword
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                // Not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // Not used in this case
            }

            override suspend fun createCacheRequestAndReturn() {
                // Not used in this case
            }

            override fun setJob(job: Job) {
               addJob("changePassword",job)
            }

        }.asLiveData()
    }


}