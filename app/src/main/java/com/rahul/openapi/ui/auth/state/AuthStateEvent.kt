package com.rahul.openapi.ui.auth.state

sealed class AuthStateEvent {

    data class LoginEvent(val email: String, val password: String) : AuthStateEvent()
    data class RegisterEvent(
        val email: String, val username: String,
        val password: String, val confirm_password: String
    ) : AuthStateEvent()

    class checkPreviousAuthEvent : AuthStateEvent()

    class None:AuthStateEvent()
}