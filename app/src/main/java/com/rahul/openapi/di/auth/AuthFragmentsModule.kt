package com.rahul.openapi.di.auth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import com.rahul.openapi.fragments.auth.AuthFragmentFactory
import dagger.Module
import dagger.Provides

@Module
object AuthFragmentsModule {

    @JvmStatic
    @AuthScope
    @Provides
    fun provideFragmentFactory(
        viewModelProviderFactory: ViewModelProvider.Factory
    ): FragmentFactory {
        return AuthFragmentFactory(viewModelProviderFactory)
    }
}