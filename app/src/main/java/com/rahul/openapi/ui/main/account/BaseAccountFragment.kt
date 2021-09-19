package com.rahul.openapi.ui.main.account

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
import com.rahul.openapi.ui.main.MainDependencyProvider
import com.rahul.openapi.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.rahul.openapi.ui.main.account.state.AccountViewState
import java.lang.ClassCastException
import java.lang.Exception


abstract class BaseAccountFragment : Fragment(), Injectable {

    lateinit var dataStateChangeListener: DataStateChangeListener

    lateinit var dependencyProvider: MainDependencyProvider


    lateinit var viewModel: AccountViewModel
    private val TAG = "AppDebug"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            dataStateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement DataStateChangeListener ")
        }
        try {
            dependencyProvider = context as MainDependencyProvider
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement MainDependencyProvider ")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::viewModel.isInitialized) {
            outState.putParcelable(ACCOUNT_VIEW_STATE_BUNDLE_KEY, viewModel.viewState.value)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this, dependencyProvider.getViewModelProviderFactory()).get(
                AccountViewModel::class.java
            )
        } ?: throw Exception("Invalid Activity")

        cancelActiveJobs()

        savedInstanceState?.let { state ->
            (state[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let {
                viewModel.setViewState(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.accountFragment, activity as AppCompatActivity)

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