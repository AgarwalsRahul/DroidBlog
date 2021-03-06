package com.rahul.openapi.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import com.rahul.openapi.R
import com.rahul.openapi.models.AccountProperties
import com.rahul.openapi.ui.main.account.state.AccountStateEvent
import kotlinx.android.synthetic.main.fragment_update_account.*


class UpdateAccountFragment : BaseAccountFragment() {
    private val TAG = "AppDebug"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_account, container, false)

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