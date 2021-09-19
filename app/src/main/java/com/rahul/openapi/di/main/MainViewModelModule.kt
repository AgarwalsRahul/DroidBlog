package com.rahul.openapi.di.main

import androidx.lifecycle.ViewModel
import com.rahul.openapi.di.ViewModelKey
import com.rahul.openapi.ui.main.account.AccountViewModel
import com.rahul.openapi.ui.main.blog.viewModel.BlogViewModel
import com.rahul.openapi.ui.main.create_blog.CreateBlogViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel::class)
    abstract fun bindBlogViewModel(blogViewModel: BlogViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(CreateBlogViewModel::class)
    abstract fun bindCreateBlogViewModel(createBlogViewModel: CreateBlogViewModel): ViewModel
}