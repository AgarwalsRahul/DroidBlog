package com.rahul.openapi.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.rahul.openapi.R
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.models.AccountProperties
import com.rahul.openapi.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.rahul.openapi.ui.main.account.state.AccountStateEvent
import com.rahul.openapi.ui.main.account.state.AccountViewState
import kotlinx.android.synthetic.main.fragment_update_account.*
import javax.inject.Inject


@MainScope
class UpdateAccountFragment @Inject constructor(
    private val viewModelProviderFactory: ViewModelProvider.Factory,
) : BaseAccountFragment(R.layout.fragment_update_account ) {

    private val TAG = "AppDebug"

    val viewModel : AccountViewModel by viewModels {
        viewModelProviderFactory
    }



    override fun onSaveInstanceState(outState: Bundle) {

        outState.putParcelable(ACCOUNT_VIEW_STATE_BUNDLE_KEY, viewModel.viewState.value)

        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        cancelActiveJobs()

        savedInstanceState?.let { state ->
            (state[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let {
                viewModel.setViewState(it)
            }
        }
    }

    override fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObserver()
    }

    private fun subscribeObserver() {

        viewModel.dataState.observe(viewLifecycleOwner, {
            dataStateChangeListener.onDataStateChanged(it)
        })
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.let {
                it.accountProperties?.let { accountProperties ->
                    Log.d(TAG, "UpdateAccountFragment: $accountProperties")
                    setAccountDetails(accountProperties)
                }
            }
        })
    }

    private fun setAccountDetails(accountProperties: AccountProperties) {
        input_email?.let {
            input_email.setText(accountProperties.email)
        }
        input_username?.let {
            input_username.setText(accountProperties.username)
        }
    }

    private fun saveChanges() {
        viewModel.setStateEvent(
            AccountStateEvent.UpdateAccountPropertiesEvent(
                input_email.text.toString(),
                input_username.text.toString()
            )
        )
        dataStateChangeListener.hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}