package com.rahul.openapi.di.main

import javax.inject.Scope
/*
    Main Scope is strictly for Blog and Account related work
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MainScope()
