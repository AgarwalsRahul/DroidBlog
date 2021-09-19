package com.rahul.openapi.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.bumptech.glide.RequestManager
import com.rahul.openapi.R
import com.rahul.openapi.models.AUTH_TOKEN_BUNDLE_KEY
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.ui.BaseActivity
import com.rahul.openapi.ui.auth.AuthActivity
import com.rahul.openapi.ui.main.account.BaseAccountFragment
import com.rahul.openapi.ui.main.account.ChangePasswordFragment
import com.rahul.openapi.ui.main.account.UpdateAccountFragment
import com.rahul.openapi.ui.main.blog.BaseBlogFragment
import com.rahul.openapi.ui.main.blog.UpdateBlogFragment
import com.rahul.openapi.ui.main.blog.ViewBlogFragment
import com.rahul.openapi.ui.main.create_blog.BaseCreateBlogFragment
import com.rahul.openapi.util.BottomNavController
import com.rahul.openapi.util.setUpNavigation
import com.rahul.openapi.viewModels.ViewModelProviderFactory
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progress_bar
import javax.inject.Inject

class MainActivity : BaseActivity(),
    BottomNavController.NavGraphProvider,
    BottomNavController.OnNavigationGraphChanged,
    BottomNavController.OnNavigationReselectedListener, MainDependencyProvider {
    private val TAG = "AppDebug"
    private lateinit var bottomNavigationView: BottomNavigationView

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var requestManager: RequestManager


    override fun getViewModelProviderFactory() = providerFactory

    override fun getGlideRequestManager() = requestManager


    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_nav_host_fragment,
            R.id.nav_blog,
            this,
            this
        )
    }

    override fun getNavGraphId(itemId: Int) = when (itemId) {
        R.id.nav_blog -> {
            R.navigation.nav_blog
        }
        R.id.nav_create_blog -> {
            R.navigation.nav_create_blog
        }
        R.id.nav_account -> {
            R.navigation.nav_account
        }
        else -> {
            R.navigation.nav_blog
        }
    }

    override fun onGraphChange() {
        cancelActiveJobs()
        expandAppBar()
    }

    private fun cancelActiveJobs() {
        val fragments = bottomNavController.fragmentManager
            .findFragmentById(bottomNavController.containerId)
            ?.childFragmentManager
            ?.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment is BaseAccountFragment) {
                    fragment.cancelActiveJobs()
                }
                if (fragment is BaseBlogFragment) {
                    fragment.cancelActiveJobs()
                }
                if (fragment is BaseCreateBlogFragment) {
                    fragment.cancelActiveJobs()
                }
            }
        }
        displayProgressBar(false)
    }

    override fun onReselectNavItem(
        navController: NavController,
        fragment: Fragment
    ) = when (fragment) {

        is ViewBlogFragment -> {
            navController.navigate(R.id.action_viewBlogFragment_to_home)
        }

        is UpdateBlogFragment -> {
            navController.navigate(R.id.action_updateBlogFragment_to_home)
        }

        is UpdateAccountFragment -> {
            navController.navigate(R.id.action_updateAccountFragment_to_home)
        }

        is ChangePasswordFragment -> {
            navController.navigate(R.id.action_changePasswordFragment_to_home)
        }

        else -> {
            // do nothing
        }
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(AUTH_TOKEN_BUNDLE_KEY,sessionManager.cachedToken.value)
        super.onSaveInstanceState(outState)
    }

    private fun restoreSession(saverInstanceBundle: Bundle?){
        saverInstanceBundle?.let { inState->
            (inState[AUTH_TOKEN_BUNDLE_KEY] as AuthToken?) ?.let {
                sessionManager.setValue(it)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.onNavigationItemSelected()
        }
        restoreSession(savedInstanceState)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Log.d(TAG, "MainActivity, subscribeObservers: ViewState: ${authToken}")
            if (authToken == null || authToken.account_pk == -1 || authToken.token == null) {
                navAuthActivity()
                finish()
            }
        })
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    private fun setupActionBar() {
        setSupportActionBar(tool_bar)
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(boolean: Boolean) {
        if (boolean) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }


}