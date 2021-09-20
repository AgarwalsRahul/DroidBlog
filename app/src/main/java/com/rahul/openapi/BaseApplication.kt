package com.rahul.openapi

import android.app.Application
import com.rahul.openapi.di.AppComponent
import com.rahul.openapi.di.DaggerAppComponent
import com.rahul.openapi.di.auth.AuthComponent
import com.rahul.openapi.di.main.MainComponent

class BaseApplication : Application() {


    lateinit var appComponent: AppComponent

    private var authComponent: AuthComponent? = null
    private var mainComponent: MainComponent? = null

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    fun authComponent(): AuthComponent {
        if (authComponent == null) {
            authComponent = appComponent.authComponent().create()
        }
        return authComponent as AuthComponent
    }

    fun releaseAuthComponent() {
        authComponent = null
    }

    fun mainComponent(): MainComponent {
        if (authComponent == null) {
            mainComponent = appComponent.mainComponent().create()
        }
        return mainComponent as MainComponent
    }

    fun releaseMainComponent() {
        mainComponent = null
    }

    fun initAppComponent(){
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

}