package com.rahul.openapi.di.main

import com.rahul.openapi.api.main.OpenApiMainService
import com.rahul.openapi.persistence.AccountPropertiesDao
import com.rahul.openapi.persistence.AppDatabase
import com.rahul.openapi.persistence.BlogPostDao
import com.rahul.openapi.repository.main.AccountRepository
import com.rahul.openapi.repository.main.BlogRepository
import com.rahul.openapi.repository.main.CreateBlogRepository
import com.rahul.openapi.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

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
    ): AccountRepository {
        return AccountRepository(accountPropertiesDao, openApiMainService, sessionManager)
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
    ): BlogRepository {
        return BlogRepository(openApiMainService, blogPostDao, sessionManager)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository(openApiMainService, blogPostDao, sessionManager)
    }
}