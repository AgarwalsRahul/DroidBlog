package com.rahul.openapi.di

import androidx.lifecycle.ViewModelProvider
import com.rahul.openapi.viewModels.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}