package com.rahul.openapi.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.rahul.openapi.R
//import com.codingwithmitch.openapi.R
import kotlinx.android.synthetic.main.fragment_launcher.*


class LauncherFragment : BaseAuthFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_launcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login.setOnClickListener {
            findNavController().navigate(R.id.action_launcherFragment_to_loginFragment)
        }
        register.setOnClickListener {
            findNavController().navigate(R.id.action_launcherFragment_to_registerFragment)
        }
        forgot_password.setOnClickListener {
            findNavController().navigate(R.id.action_launcherFragment_to_forgotPasswordFragment)
        }
    }


}