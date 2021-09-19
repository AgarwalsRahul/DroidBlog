package com.rahul.openapi.ui.main.blog

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.rahul.openapi.R
import com.rahul.openapi.models.BlogPost
import com.rahul.openapi.persistence.BlogQueryUtils.Companion.BLOG_FILTER_DATE_UPDATED
import com.rahul.openapi.persistence.BlogQueryUtils.Companion.BLOG_FILTER_USERNAME
import com.rahul.openapi.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.main.blog.state.BlogViewState
import com.rahul.openapi.ui.main.blog.viewModel.*
import com.rahul.openapi.util.ErrorHandling
import com.rahul.openapi.util.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_blog.*


class BlogFragment : BaseBlogFragment(), BlogListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    private val TAG = "`AppDebug`"


    private lateinit var recyclerAdapter: BlogListAdapter
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()

//        if (savedInstanceState == null) {
//            viewModel.loadFirstPage()
//        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.restoreFromCache()
    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    private fun saveLayoutManagerState() {
        blog_post_recyclerview.layoutManager?.onSaveInstanceState()?.let {
            viewModel.setLayoutManagerState(it)
        }
    }

    private fun onBlogSearchOrFilter() {
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        blog_post_recyclerview.smoothScrollToPosition(0)
        dataStateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                handlePagination(dataState)
                dataStateChangeListener.onDataStateChanged(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            Log.d(TAG, "BlogFragment, ViewState: $viewState")
            viewState?.let {
                recyclerAdapter.apply {
                    preloadGlideImages(
                        dependencyProvider.getGlideRequestManager(),
                        it.blogFields.blogList
                    )
                    Log.d(TAG, "List Items : ${viewState.blogFields.blogList.size}")
                    submitList(it.blogFields.blogList, it.blogFields.isQueryExhausted)
                }
            }

        })
    }

    private fun handlePagination(dataState: DataState<BlogViewState>) {
        // Handle incoming data from dataState
        dataState.data?.let {
            it.data?.let { event ->
                event.getContentIfNotHandled()?.let { viewState ->
                    viewModel.handleIncomingBlogListData(viewState)
                }
            }
        }

        // Check fo pagination end i.e no more result
        // must do this because api will return error response if page is not valid

        dataState.error?.let { event ->
            event.peekContent().response.message?.let {
                if (ErrorHandling.isPaginationDone(it)) {
                    // handle the event so that it doesn't effect ui
                    event.getContentIfNotHandled()
                    viewModel.setQueryExhausted(true)
                }
            }
        }
    }

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
            searchView.setOnQueryTextListener(this@BlogFragment)
        }

//        // ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
//        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
//        searchPlate.setOnEditorActionListener { v, actionId, event ->
//
//            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
//                || actionId == EditorInfo.IME_ACTION_SEARCH
//            ) {
//                val searchQuery = v.text.toString()
//                Log.e(TAG, "SearchView: (keyboard or arrow) executing search...: $searchQuery")
//                viewModel.setQuery(searchQuery).let {
//                    onBlogSearchOrFilter()
//                }
//            }
//            true
//        }
//
//        // SEARCH BUTTON CLICKED (in toolbar)
//        val searchButton = searchView.findViewById(R.id.search_go_btn) as View
//        searchButton.setOnClickListener {
//            val searchQuery = searchPlate.text.toString()
//            Log.e(TAG, "SearchView: (button) executing search...: ${searchQuery}")
//            viewModel.setQuery(searchQuery).let {
//                onBlogSearchOrFilter()
//            }

//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_filter_settings -> {
                showFilterDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initRecyclerView() {

        blog_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@BlogFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter =
                BlogListAdapter(dependencyProvider.getGlideRequestManager(), this@BlogFragment)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "BlogFragment: attempting to load next page...")
                        viewModel.nextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }

    }

    override fun onItemSelected(position: Int, item: BlogPost) {
        Log.d(TAG, "onItemSelected: position, BlogPost: $position, $item")
        viewModel.setBlogPost(item)
        findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)
    }

    override fun restoreListPosition() {
       viewModel.viewState.value?.blogFields?.layoutManagerState?.let {
           blog_post_recyclerview?.layoutManager?.onRestoreInstanceState(it)
       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        blog_post_recyclerview.adapter = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onRefresh() {
        onBlogSearchOrFilter()
        swipe_refresh.isRefreshing = false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            Log.e(TAG, "SearchView: (keyboard or arrow) executing search...: $query")
            viewModel.setQuery(query).let {
                onBlogSearchOrFilter()
            }
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            viewModel.setQuery(newText).let {
                viewModel.loadFirstPage().let {
                    blog_post_recyclerview.smoothScrollToPosition(0)
                }
            }
        }
        return true
    }

    private fun showFilterDialog() {

        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_blog_filter)

            val view = dialog.getCustomView()

            val filter = viewModel.getFilter()
            val order = viewModel.getOrder()

            if (filter == BLOG_FILTER_DATE_UPDATED) {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_date)
            } else {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_author)
            }

            if (order == BLOG_ORDER_ASC) {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_asc)
            } else {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_desc)
            }

            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: apply filter.")

                val selectedFilter = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView()
                        .findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
                )
                val selectedOrder = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView()
                        .findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
                )

                var filter = BLOG_FILTER_DATE_UPDATED
                if (selectedFilter.text.toString().equals(getString(R.string.filter_author))) {
                    filter = BLOG_FILTER_USERNAME
                }

                var order = ""
                if (selectedOrder.text.toString().equals(getString(R.string.filter_desc))) {
                    order = "-"
                }
                viewModel.saveFilterOptions(filter, order).let {
                    viewModel.setFilter(filter)
                    viewModel.setOrder(order)
                    onBlogSearchOrFilter()
                }
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: cancelling filter.")
                dialog.dismiss()
            }

            dialog.show()
        }
    }

}