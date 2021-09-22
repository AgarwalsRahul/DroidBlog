package com.rahul.openapi.di.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rahul.openapi.di.auth.keys.AuthViewModelKey
import com.rahul.openapi.ui.auth.AuthViewModel
import com.rahul.openapi.viewModels.AuthViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
abstract class AuthViewModelModule {

    @AuthScope
    @Binds
    abstract fun bindViewModelFactory(authViewModelFactory: AuthViewModelFactory): ViewModelProvider.Factory

    @ExperimentalCoroutinesApi
    @AuthScope
    @Binds
    @IntoMap
    @AuthViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}