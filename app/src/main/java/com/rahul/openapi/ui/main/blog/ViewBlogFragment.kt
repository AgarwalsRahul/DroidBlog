package com.rahul.openapi.ui.main.blog

import android.os.Bundle
import android.view.*

import androidx.navigation.fragment.findNavController
import com.rahul.openapi.R
import com.rahul.openapi.models.BlogPost
import com.rahul.openapi.ui.AreyouSureCallback
import com.rahul.openapi.ui.UIMessage
import com.rahul.openapi.ui.UIMessageType
import com.rahul.openapi.ui.main.blog.state.BlogStateEvent.*
import com.rahul.openapi.ui.main.blog.viewModel.getIsAuthorOfBlogPost
import com.rahul.openapi.ui.main.blog.viewModel.removeDeletedBlogPost
import com.rahul.openapi.ui.main.blog.viewModel.setAuthorOfBlog
import com.rahul.openapi.util.DateUtils
import com.rahul.openapi.util.SuccessHandling
import kotlinx.android.synthetic.main.fragment_view_blog.*


class ViewBlogFragment : BaseBlogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_blog, container, false)
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
        dependencyProvider.getGlideRequestManager().load(blogPost.image).into(blog_image)

        blog_title.text = blogPost.title
        blog_author.text = blogPost.username
        blog_update_date.text = DateUtils.convertLongToStringDate(blogPost.date_updated)
        blog_body.text = blogPost.body
    }

    private fun navUpdateBlogFragment() {
        findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
    }
}