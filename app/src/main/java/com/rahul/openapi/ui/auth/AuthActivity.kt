package com.rahul.openapi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.rahul.openapi.R
import com.rahul.openapi.ui.BaseActivity
import com.rahul.openapi.ui.auth.state.AuthStateEvent
import com.rahul.openapi.ui.main.MainActivity
import com.rahul.openapi.viewModels.ViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_auth.*
import javax.inject.Inject

class AuthActivity : BaseActivity() {
    companion object {
        private const val TAG = "AppDebug"
    }

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        subscribeObserver()
        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener { _, _, _ ->
            viewModel.cancelActiveJobs()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    private fun subscribeObserver() {
        viewModel.dataState.observe(this, {
            onDataStateChanged(it)
            it?.let {
                it.data?.let { data ->
                    data.data?.let { event ->
                        event.getContentIfNotHandled()?.let { authViewState ->
                            authViewState.authToken?.let { token ->

                                viewModel.setAuthToken(token)
                            }
                        }
                    }
                }


            }
        })

        viewModel.viewState.observe(this, { authViewState ->
            authViewState.authToken?.let {
                sessionManager.login(it)
            }
        })
        sessionManager.cachedToken.observe(this, { authToken ->
            if (authToken != null && authToken.account_pk != -1 && authToken.token != null) {
                navMainActivity()

            }
        })
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkPreviousAuthUser() {
        viewModel.setStateEvent(AuthStateEvent.checkPreviousAuthEvent())
    }

    override fun displayProgressBar(boolean: Boolean) {
        if (boolean) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.INVISIBLE
        }
    }

    override fun expandAppBar() {

    }
}