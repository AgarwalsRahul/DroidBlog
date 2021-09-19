package com.rahul.openapi.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.rahul.openapi.R
import com.rahul.openapi.di.Injectable
import com.rahul.openapi.ui.DataStateChangeListener
import com.rahul.openapi.ui.UICommunicationListener
import com.rahul.openapi.ui.main.MainDependencyProvider
import com.rahul.openapi.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.rahul.openapi.ui.main.blog.state.BlogViewState
import com.rahul.openapi.ui.main.blog.viewModel.BlogViewModel

abstract class BaseBlogFragment : Fragment(), Injectable {


    lateinit var viewModel: BlogViewModel
    lateinit var dataStateChangeListener: DataStateChangeListener

    lateinit var uiCommunicationListener: UICommunicationListener

    lateinit var dependencyProvider: MainDependencyProvider

    private val TAG = "AppDebug"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, dependencyProvider.getViewModelProviderFactory()).get(
                BlogViewModel::class.java
            )
        } ?: throw Exception("Invalid Activity")
        cancelActiveJobs()

        // Restore state after process death
        savedInstanceState?.let { state ->
            (state[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState?)?.let {
                viewModel.setViewState(it)
            }
        }

    }

    private fun isViewModelInitialized() = ::viewModel.isInitialized

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            dataStateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement DataStateChangeListener ")
        }
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener ")
        }
        try {
            dependencyProvider = context as MainDependencyProvider
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement MainDependencyProvider ")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (isViewModelInitialized()) {
            val viewState = viewModel.viewState.value
            viewState?.let {
                it.blogFields?.let { blog->
                    blog.blogList=ArrayList()
                }
            }
            outState.putParcelable(BLOG_VIEW_STATE_BUNDLE_KEY,viewState)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.blogFragment, activity as AppCompatActivity)

    }

    private fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )

    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }
}