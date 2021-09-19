package com.rahul.openapi.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rahul.openapi.R
import com.rahul.openapi.ui.auth.state.AuthStateEvent
import com.rahul.openapi.ui.auth.state.LoginFields
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : BaseAuthFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()

        login_button.setOnClickListener {
           login()
        }
    }

    private fun login() {
        viewModel.setStateEvent(
            AuthStateEvent.LoginEvent(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.let { authViewState ->
                authViewState.loginFields?.let { loginField ->
                    loginField.login_email?.let {
                        input_email.setText(it)
                    }
                    loginField.login_password?.let {
                        input_password.setText(it)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                login_email = input_email.text.toString(),
                login_password = input_password.text.toString()
            )
        )
    }

}