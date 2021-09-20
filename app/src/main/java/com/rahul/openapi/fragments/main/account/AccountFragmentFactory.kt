package com.rahul.openapi.fragments.main.account

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.rahul.openapi.di.auth.AuthScope
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.ui.auth.ForgotPasswordFragment
import com.rahul.openapi.ui.auth.LauncherFragment
import com.rahul.openapi.ui.auth.LoginFragment
import com.rahul.openapi.ui.auth.RegisterFragment
import com.rahul.openapi.ui.main.account.AccountFragment
import com.rahul.openapi.ui.main.account.ChangePasswordFragment
import com.rahul.openapi.ui.main.account.UpdateAccountFragment
import javax.inject.Inject

@MainScope
class AccountFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =

        when (className) {

            AccountFragment::class.java.name -> {
                AccountFragment(viewModelFactory)
            }

            ChangePasswordFragment::class.java.name -> {
                ChangePasswordFragment(viewModelFactory)
            }

            UpdateAccountFragment::class.java.name -> {
                UpdateAccountFragment(viewModelFactory)
            }


            else -> {
                AccountFragment(viewModelFactory)
            }
        }


}