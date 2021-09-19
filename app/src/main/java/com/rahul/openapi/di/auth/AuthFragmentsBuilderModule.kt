package com.rahul.openapi.di.auth

import com.rahul.openapi.ui.auth.ForgotPasswordFragment
import com.rahul.openapi.ui.auth.LauncherFragment
import com.rahul.openapi.ui.auth.LoginFragment
import com.rahul.openapi.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module

abstract class AuthFragmentsBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeLauncherFragment():LauncherFragment

    @ContributesAndroidInjector
    abstract fun contributeLoginFragment():LoginFragment

    @ContributesAndroidInjector
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector
    abstract fun contributeForgotPasswordFragment():ForgotPasswordFragment
}