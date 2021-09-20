package com.rahul.openapi.di

import android.app.Application
import com.rahul.openapi.di.auth.AuthComponent
import com.rahul.openapi.di.main.MainComponent
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.ui.BaseActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        AppModule::class, SubComponentsModule::class
    ]
)
interface AppComponent {

    val sessionManager: SessionManager // must add here b/c injecting into abstract class

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(baseActivity: BaseActivity)

    fun authComponent(): AuthComponent.Factory

    fun mainComponent(): MainComponent.Factory
}