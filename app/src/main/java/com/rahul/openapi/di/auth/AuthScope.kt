package com.rahul.openapi.di.auth

import javax.inject.Scope
/*
    Auth Scope is strictly for login and registration
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthScope()
