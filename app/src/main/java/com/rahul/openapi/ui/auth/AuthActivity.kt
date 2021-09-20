package com.rahul.openapi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.rahul.openapi.BaseApplication
import com.rahul.openapi.R
import com.rahul.openapi.fragments.auth.AuthFragmentFactory
import com.rahul.openapi.fragments.auth.AuthNavHostFragment
import com.rahul.openapi.ui.BaseActivity
import com.rahul.openapi.ui.auth.state.AuthStateEvent
import com.rahul.openapi.ui.main.MainActivity
import com.rahul.openapi.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.android.synthetic.main.activity_auth.*
import javax.inject.Inject

class AuthActivity : BaseActivity() {
    companion object {
        private const val TAG = "AppDebug"
    }

    @Inject
    lateinit var fragmentFactory: AuthFragmentFactory


    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    val viewModel: AuthViewModel by viewModels {
        providerFactory
    }

    override fun inject() {
        (application as BaseApplication).authComponent().inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        inject()



        super.onCreate(savedInstanceState)
        Log.d(TAG, "$fragmentFactory")
        setContentView(R.layout.activity_auth)

        subscribeObserver()
        onRestoreInstanceSate()
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
                    data.response?.let { event ->
                        event.peekContent().let { response ->
                            response.message?.let { message ->
                                if (message.equals(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE)) {
                                    onFinishCheckPreviousAuthUser()
                                }
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

    private fun onRestoreInstanceSate() {
        val host = supportFragmentManager.findFragmentById(R.id.auth_fragments_container)
        host?.let {
            //do nothing
        } ?: createNavHost()
    }

    private fun createNavHost() {
        val navHost = AuthNavHostFragment.create(R.navigation.auth_nav_graph)
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragments_container, navHost, "AuthNavHost")
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }

    private fun onFinishCheckPreviousAuthUser() {
        fragment_container.visibility = View.VISIBLE
    }


    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        (application as BaseApplication).releaseAuthComponent()
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