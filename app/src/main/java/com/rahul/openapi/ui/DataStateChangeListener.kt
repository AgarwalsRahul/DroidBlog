package com.rahul.openapi.ui

interface DataStateChangeListener {

    fun onDataStateChanged(dataState: DataState<*>?)

    fun expandAppBar()

    fun hideSoftKeyboard()

    fun isStoragePermissionGranted():Boolean

}