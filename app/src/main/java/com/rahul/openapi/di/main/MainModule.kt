package com.rahul.openapi.di.main

import com.rahul.openapi.api.main.OpenApiMainService
import com.rahul.openapi.persistence.AccountPropertiesDao
import com.rahul.openapi.persistence.AppDatabase
import com.rahul.openapi.persistence.BlogPostDao
import com.rahul.openapi.repository.main.AccountRepositoryImpl
import com.rahul.openapi.repository.main.BlogRepositoryImpl
import com.rahul.openapi.repository.main.CreateBlogRepositoryImpl
import com.rahul.openapi.session.SessionManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit

@FlowPreview
@Module
object MainModule {
    @JvmStatic
    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder.build().create(OpenApiMainService::class.java)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideAccountRepository(
        accountPropertiesDao: AccountPropertiesDao,
        openApiMainService: OpenApiMainService,
        sessionManager: SessionManager
    ): AccountRepositoryImpl {
        return AccountRepositoryImpl(openApiMainService, accountPropertiesDao, sessionManager)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.blogPostDao
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepositoryImpl {
        return BlogRepositoryImpl(openApiMainService, blogPostDao, sessionManager)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepositoryImpl {
        return CreateBlogRepositoryImpl(openApiMainService, blogPostDao, sessionManager)
    }
}