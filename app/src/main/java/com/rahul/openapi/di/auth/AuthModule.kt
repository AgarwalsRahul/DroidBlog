package com.rahul.openapi.di.auth

import android.content.SharedPreferences
import com.rahul.openapi.api.auth.OpenApiAuthService
import com.rahul.openapi.persistence.AccountPropertiesDao
import com.rahul.openapi.persistence.AuthTokenDao
import com.rahul.openapi.repository.auth.AuthRepository
import com.rahul.openapi.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule {
    // TEMPORARY
    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder: Retrofit.Builder): OpenApiAuthService {
        return retrofitBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

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
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,
            sharedPreferences,
            sharedPreferencesEditor
        )
    }
}