package com.rahul.openapi.ui.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rahul.openapi.R
import com.rahul.openapi.di.main.MainScope
import com.rahul.openapi.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.rahul.openapi.ui.main.account.state.AccountStateEvent
import com.rahul.openapi.ui.main.account.state.AccountViewState
import com.rahul.openapi.util.SuccessHandling
import kotlinx.android.synthetic.main.fragment_change_password.*
import javax.inject.Inject

@MainScope
class ChangePasswordFragment @Inject constructor(
    private val viewModelProviderFactory: ViewModelProvider.Factory,
) : BaseAccountFragment(R.layout.fragment_change_password) {

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
        subscribeObservers()
        update_password_button.setOnClickListener {
            viewModel.setStateEvent(
                AccountStateEvent.ChangePasswordEvent(
                    input_current_password.text.toString(),
                    input_new_password.text.toString(),
                    input_confirm_new_password.text.toString(),
                )
            )
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, { dataState ->
            dataStateChangeListener.onDataStateChanged(dataState)

            dataState?.let { data ->
                data.data?.let {
                    it.response?.let { event ->
                        if (event.peekContent().message == SuccessHandling.RESPONSE_PASSWORD_UPDATE_SUCCESS) {
                            dataStateChangeListener.hideSoftKeyboard()
                            findNavController().popBackStack()
                        }
                    }
                }
            }

        })

        viewModel.viewState.observe(viewLifecycleOwner, {})
    }
}