package com.rahul.openapi.ui.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.rahul.openapi.R
import com.rahul.openapi.ui.main.account.state.AccountStateEvent
import com.rahul.openapi.util.SuccessHandling
import kotlinx.android.synthetic.main.fragment_change_password.*


class ChangePasswordFragment : BaseAccountFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
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