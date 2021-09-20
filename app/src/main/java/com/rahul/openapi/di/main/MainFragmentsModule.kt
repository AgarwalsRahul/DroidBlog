package com.rahul.openapi.di.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.rahul.openapi.fragments.auth.AuthFragmentFactory
import com.rahul.openapi.fragments.main.account.AccountFragmentFactory
import com.rahul.openapi.fragments.main.blog.BlogFragmentFactory
import com.rahul.openapi.fragments.main.create_blog.CreateBlogFragmentFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object MainFragmentsModule {

    @JvmStatic
    @MainScope
    @Provides
    @Named("AccountFragmentFactory")
    fun provideAccountFragmentFactory(
        viewModelProviderFactory: ViewModelProvider.Factory
    ): FragmentFactory {
        return AccountFragmentFactory(viewModelProviderFactory)
    }

    @JvmStatic
    @MainScope
    @Provides
    @Named("BlogFragmentFactory")
    fun provideBlogFragmentFactory(
        viewModelProviderFactory: ViewModelProvider.Factory,
        requestManager: RequestManager
    ): FragmentFactory {
        return BlogFragmentFactory(viewModelProviderFactory, requestManager)
    }

    @JvmStatic
    @MainScope
    @Provides
    @Named("CreateBlogFragmentFactory")
    fun provideCreateBlogFragmentFactory(
        viewModelProviderFactory: ViewModelProvider.Factory,
        requestManager: RequestManager
    ): FragmentFactory {
        return CreateBlogFragmentFactory(viewModelProviderFactory, requestManager)
    }
}