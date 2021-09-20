package com.rahul.openapi.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.rahul.openapi.R
import com.rahul.openapi.ui.DataStateChangeListener
import com.rahul.openapi.ui.UICommunicationListener
import com.rahul.openapi.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.rahul.openapi.ui.main.blog.state.BlogViewState
import com.rahul.openapi.ui.main.blog.viewModel.BlogViewModel

abstract class BaseBlogFragment constructor(@LayoutRes private val layoutRes: Int) : Fragment(
    layoutRes
) {


    lateinit var dataStateChangeListener: DataStateChangeListener

    lateinit var uiCommunicationListener: UICommunicationListener


    private val TAG = "AppDebug"




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

    abstract fun cancelActiveJobs()
}