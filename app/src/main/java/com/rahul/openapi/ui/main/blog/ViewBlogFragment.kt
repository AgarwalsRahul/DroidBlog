package com.rahul.openapi.ui.main.blog

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.RequestManager
import com.rahul.openapi.R
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.models.BlogPost
import com.rahul.openapi.ui.AreyouSureCallback
import com.rahul.openapi.ui.UIMessage
import com.rahul.openapi.ui.UIMessageType
import com.rahul.openapi.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.rahul.openapi.ui.main.blog.state.BlogStateEvent.*
import com.rahul.openapi.ui.main.blog.state.BlogViewState
import com.rahul.openapi.ui.main.blog.viewModel.BlogViewModel
import com.rahul.openapi.ui.main.blog.viewModel.getIsAuthorOfBlogPost
import com.rahul.openapi.ui.main.blog.viewModel.removeDeletedBlogPost
import com.rahul.openapi.ui.main.blog.viewModel.setAuthorOfBlog
import com.rahul.openapi.util.DateUtils
import com.rahul.openapi.util.SuccessHandling
import kotlinx.android.synthetic.main.fragment_view_blog.*
import javax.inject.Inject


@MainScope

class ViewBlogFragment @Inject constructor(
    private val viewModelProviderFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) :
    BaseBlogFragment(R.layout.fragment_view_blog) {
    private val TAG = "`AppDebug`"


    val viewModel: BlogViewModel by viewModels {
        viewModelProviderFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cancelActiveJobs()

        // Restore state after process death
        savedInstanceState?.let { state ->
            (state[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState?)?.let {
                viewModel.setViewState(it)
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {

        val viewState = viewModel.viewState.value
        viewState?.let {
            it.blogFields?.let { blog ->
                blog.blogList = ArrayList()

            }
            outState.putParcelable(BLOG_VIEW_STATE_BUNDLE_KEY, viewState)
        }

        super.onSaveInstanceState(outState)
    }


    override fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        checkIsAuthorOfBlogPost()
        delete_button.setOnClickListener {
            confirmDeleteRequest()
        }
//        dataStateChangeListener.expandAppBar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        if (viewModel.getIsAuthorOfBlogPost()) {
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        if (viewModel.getIsAuthorOfBlogPost()) {
            when (item.itemId) {
                R.id.edit -> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmDeleteRequest() {

        val callback = object : AreyouSureCallback {
            override fun cancel() {
                //Ignore
            }

            override fun proceed() {
                viewModel.setStateEvent(DeleteBlogPostEvent())
            }

        }
        uiCommunicationListener.onUIMessageRecieved(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }


    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, { dataState ->
            if (dataState != null) {
                dataStateChangeListener.onDataStateChanged(dataState)
                dataState.data?.let { data ->
                    data.data?.getContentIfNotHandled()?.let { viewState ->
                        viewModel.setAuthorOfBlog(
                            viewState.viewBlogFields.isAuthorOfPost
                        )
                    }
                    data.response?.peekContent()?.let {
                        if (it.message == SuccessHandling.SUCCESS_BLOG_DELETED) {
                            viewModel.removeDeletedBlogPost()
                            findNavController().popBackStack()
                        }
                    }
                }
            }

        })

        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState.viewBlogFields.blogPost?.let {
                setBlogProperties(it)
            }
            if (viewState.viewBlogFields.isAuthorOfPost) {
                adaptViewToAuthorMode()
            }
        })
    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        delete_button.visibility = View.VISIBLE
    }

    private fun checkIsAuthorOfBlogPost() {
        viewModel.setAuthorOfBlog(false) // reset
        viewModel.setStateEvent(CheckAuthorOfBlogPost())
    }

    private fun setBlogProperties(blogPost: BlogPost) {
      requestManager.load(blogPost.image).into(blog_image)

        blog_title.text = blogPost.title
        blog_author.text = blogPost.username
        blog_update_date.text = DateUtils.convertLongToStringDate(blogPost.date_updated)
        blog_body.text = blogPost.body
    }

    private fun navUpdateBlogFragment() {
        findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
    }
}