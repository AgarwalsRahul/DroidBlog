package com.rahul.openapi.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.rahul.openapi.api.auth.OpenApiAuthService
import com.rahul.openapi.persistence.AccountPropertiesDao
import com.rahul.openapi.persistence.AuthTokenDao
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.auth.state.AuthViewState
import javax.inject.Inject
import com.rahul.openapi.api.auth.network_responses.LoginResponse
import com.rahul.openapi.api.auth.network_responses.RegistrationResponse
import com.rahul.openapi.models.AccountProperties
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.repository.JobManager
import com.rahul.openapi.repository.NetworkBoundResource
import com.rahul.openapi.ui.Response
import com.rahul.openapi.ui.ResponseType
import com.rahul.openapi.ui.auth.state.LoginFields
import com.rahul.openapi.ui.auth.state.RegistrationFields
import com.rahul.openapi.util.AbsentLiveData
import com.rahul.openapi.util.ErrorHandling.ERROR_SAVE_AUTH_TOKEN
import com.rahul.openapi.util.ErrorHandling.GENERIC_AUTH_ERROR
import com.rahul.openapi.util.GenericApiResponse
import com.rahul.openapi.util.GenericApiResponse.*
import com.rahul.openapi.util.PreferenceKeys
import com.rahul.openapi.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Job

class AuthRepository @Inject constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    private val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPreferencesEditor: SharedPreferences.Editor,
): JobManager("AuthRepository") {



    private val TAG = "AppDebug"

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFields = LoginFields(email, password)
        if (loginFields.isValidForLogin() != LoginFields.LoginError.none()) {
            return returnErrorResponse(loginFields.isValidForLogin(), ResponseType.Dialog())
        }
        return object : NetworkBoundResource<LoginResponse, Any, AuthViewState>(
            sessionManager.checkNetworkConnection(), true, false, true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                if (response.body.response == GENERIC_AUTH_ERROR) {
                    return onErrorReturn(response.body.errorMessage, ResponseType.Dialog())
                }
                // Just insert because of the foreign key relationship
                accountPropertiesDao.insertAndIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        "",

                        )
                )
                val result = authTokenDao.insert(AuthToken(response.body.pk, response.body.token))
                if (result < 0) {
                    return onErrorReturn(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                }
                saveAuthenticatedUserToPrefs(response.body.email)

                onCompleteJob(
                    DataState.data(
                        AuthViewState(
                            authToken = AuthToken(
                                response.body.pk,
                                response.body.token
                            )
                        ),

                        )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                addJob("attemptLogin",job)
            }

            override suspend fun createCacheRequestAndReturn() {
                // Not Required in this case
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                // Not Required in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // Not Required in this case
            }

        }.asLiveData()
    }

    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType
    ): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(Response(errorMessage, responseType))
            }
        }
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
    ): LiveData<DataState<AuthViewState>> {
        val registrationFields = RegistrationFields(email, username, password, confirmPassword)
        if (registrationFields.isValidForRegistration() != RegistrationFields.RegistrationError.none()) {
            return returnErrorResponse(
                registrationFields.isValidForRegistration(),
                ResponseType.Dialog()
            )
        }
        return object :
            NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
                sessionManager.checkNetworkConnection(),
                true, false, true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                if (response.body.response == GENERIC_AUTH_ERROR) {
                    return onErrorReturn(response.body.errorMessage, ResponseType.Dialog())
                }
                // Just insert because of the foreign key relationship
                accountPropertiesDao.insertAndIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        "",

                        )
                )
                val result = authTokenDao.insert(AuthToken(response.body.pk, response.body.token))
                if (result < 0) {
                    return onErrorReturn(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                }
                Log.d(TAG, "Inserting auth token result: $result")
                saveAuthenticatedUserToPrefs(response.body.email)
                onCompleteJob(
                    DataState.data(
                        AuthViewState(authToken = AuthToken(response.body.pk, response.body.token))
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                addJob("attemptRegistration",job)
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                // Not Required in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // Not Required in this case
            }

            override suspend fun createCacheRequestAndReturn() {
                // Not Required in this case
            }
        }.asLiveData()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)
        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found .. ")
            return returnNoTokenFound()
        }
        return object : NetworkBoundResource<Void, Any, AuthViewState>(
            sessionManager.checkNetworkConnection(),
            false, false, false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                // Not required in this case
            }

            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                // Not required in this case
                return AbsentLiveData.create()
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                // Not Required in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // Not Required in this case
            }

            override suspend fun createCacheRequestAndReturn() {
                val accountProperties = accountPropertiesDao.searchByEmail(previousAuthUserEmail)
                accountProperties?.let {
                    if (it.pk > -1) {
                        authTokenDao.searchByPk(it.pk).let { authToken ->
                            if (authToken != null) {
                                return onCompleteJob(DataState.data(AuthViewState(authToken = authToken)))
                            }
                        }
                    }
                }
                Log.d(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                onCompleteJob(
                    DataState.data(
                        null,
                        Response(
                            RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                            ResponseType.None()
                        )
                    )
                )
            }

            override fun setJob(job: Job) {
                addJob("checkPreviousAuthUser",job)
            }

        }.asLiveData()
    }




    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPreferencesEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPreferencesEditor.apply()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    null,
                    response = Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }
}