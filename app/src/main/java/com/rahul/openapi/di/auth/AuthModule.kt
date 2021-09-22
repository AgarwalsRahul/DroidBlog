package com.rahul.openapi.di.auth

import android.content.SharedPreferences
import com.rahul.openapi.api.auth.OpenApiAuthService
import com.rahul.openapi.persistence.AccountPropertiesDao
import com.rahul.openapi.persistence.AuthTokenDao
import com.rahul.openapi.repository.auth.AuthRepository
import com.rahul.openapi.repository.auth.AuthRepositoryImpl
import com.rahul.openapi.session.SessionManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit

@FlowPreview
@Module
object AuthModule {
    // TEMPORARY

    @JvmStatic
    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder: Retrofit.Builder): OpenApiAuthService {
        return retrofitBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @JvmStatic
    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: OpenApiAuthService,
        sharedPreferences: SharedPreferences,
        sharedPreferencesEditor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepositoryImpl(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,
            sharedPreferences,
            sharedPreferencesEditor
        )
    }
}