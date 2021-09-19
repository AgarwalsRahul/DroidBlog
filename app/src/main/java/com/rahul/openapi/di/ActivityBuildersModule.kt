package com.rahul.openapi.di

import com.rahul.openapi.ui.main.MainActivity
import com.rahul.openapi.di.auth.AuthFragmentsBuilderModule
import com.rahul.openapi.di.auth.AuthModule
import com.rahul.openapi.di.auth.AuthScope
import com.rahul.openapi.di.auth.AuthViewModelModule
import com.rahul.openapi.di.main.MainFragmentBuildersModule
import com.rahul.openapi.di.main.MainModule
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.di.main.MainViewModelModule
import com.rahul.openapi.ui.auth.AuthActivity

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [
            AuthModule::class,
            AuthViewModelModule::class,
            AuthFragmentsBuilderModule::class,
        ]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [
            MainModule::class,
            MainViewModelModule::class,
            MainFragmentBuildersModule::class,
        ]
    )
    abstract fun contributeMainActivity(): MainActivity
}