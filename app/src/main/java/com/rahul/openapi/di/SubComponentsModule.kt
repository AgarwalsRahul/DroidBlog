package com.rahul.openapi.di

import com.rahul.openapi.di.auth.AuthComponent
import com.rahul.openapi.di.main.MainComponent
import dagger.Module


@Module(
    subcomponents = [
        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule {
}