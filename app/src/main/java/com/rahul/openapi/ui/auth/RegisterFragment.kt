package com.rahul.openapi.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rahul.openapi.R
import com.rahul.openapi.ui.auth.state.AuthStateEvent
import com.rahul.openapi.ui.auth.state.RegistrationFields


import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment : BaseAuthFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
        register_button.setOnClickListener{
            register()
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.let { authViewState ->
                authViewState.registrationFields?.let { registrationFields ->
                    registrationFields.registration_email?.let {
                        input_email.setText(it)
                    }

                    registrationFields.registration_username?.let {
                        input_username.setText(it)
                    }
                    registrationFields.registration_password?.let {
                        input_password.setText(it)
                    }
                    registrationFields.registration_confirm_password?.let {
                        input_password_confirm.setText(it)
                    }
                }
            }
        })
    }

    private fun register(){
        viewModel.setStateEvent(AuthStateEvent.RegisterEvent(
            input_email.text.toString(),
           input_username.text.toString(),
             input_password.text.toString(),
            input_password_confirm.text.toString()
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setRegistrationFields(
            RegistrationFields(
                registration_email = input_email.text.toString(),
                registration_username = input_username.text.toString(),
                registration_password = input_password.text.toString(),
                registration_confirm_password = input_password_confirm.text.toString()
            )
        )
    }
}