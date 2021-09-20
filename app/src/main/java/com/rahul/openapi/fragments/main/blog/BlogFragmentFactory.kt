package com.rahul.openapi.fragments.main.blog

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.ui.auth.ForgotPasswordFragment
import com.rahul.openapi.ui.auth.LauncherFragment
import com.rahul.openapi.ui.auth.LoginFragment
import com.rahul.openapi.ui.auth.RegisterFragment
import com.rahul.openapi.ui.main.blog.BlogFragment
import com.rahul.openapi.ui.main.blog.UpdateBlogFragment
import com.rahul.openapi.ui.main.create_blog.CreateBlogFragment
import javax.inject.Inject


@MainScope
class BlogFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =

        when (className) {

            BlogFragment::class.java.name -> {
                BlogFragment(viewModelFactory,requestManager)
            }

            CreateBlogFragment::class.java.name -> {
                CreateBlogFragment(viewModelFactory,requestManager)
            }

            UpdateBlogFragment::class.java.name -> {
                UpdateBlogFragment(viewModelFactory,requestManager)
            }

            else -> {
                BlogFragment(viewModelFactory,requestManager)
            }
        }


}