package com.rahul.openapi.ui.main

import com.bumptech.glide.RequestManager
import com.rahul.openapi.viewModels.ViewModelProviderFactory

interface MainDependencyProvider {

    fun getViewModelProviderFactory(): ViewModelProviderFactory

    fun getGlideRequestManager(): RequestManager
}