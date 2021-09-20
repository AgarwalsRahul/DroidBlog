package com.rahul.openapi.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rahul.openapi.BaseApplication
import com.rahul.openapi.session.SessionManager
import com.rahul.openapi.util.Constants

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), DataStateChangeListener,
    UICommunicationListener {
    private val TAG = "AppDebug"


    abstract  fun inject()

    @Inject
    lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent
            .inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDataStateChanged(dataState: DataState<*>?) {
        dataState?.let {
            GlobalScope.launch(Dispatchers.Main) {
                displayProgressBar(it.loading.isLoading)
                it.error?.let { errorEvent ->
                    handleStateError(errorEvent)
                }

                it.data?.let {
                    it.response?.let { responseEvent ->
                        handleStateResponse(responseEvent)
                    }
                }
            }
        }
    }

    override fun onUIMessageRecieved(uiMessage: UIMessage) {
        when (uiMessage.uiMessageType) {
            is UIMessageType.Toast -> {
                displayToast(uiMessage.message)
            }
            is UIMessageType.Dialog -> {
                displayInfoDialog(uiMessage.message)
            }
            is UIMessageType.AreYouSureDialog -> {
                areYouSureDialog(uiMessage.message, uiMessage.uiMessageType.callback)
            }
            is UIMessageType.None -> {
                Log.i(TAG, "onUIMessageReceived ${uiMessage.message}")
            }
        }
    }

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    private fun handleStateResponse(responseEvent: Event<Response>) {
        responseEvent.getContentIfNotHandled()?.let {
            when (it.responseType) {
                is ResponseType.Dialog -> displaySuccessDialog(it.message)
                is ResponseType.Toast -> it.message?.let { msg ->
                    displayToast(msg)
                }
                is ResponseType.None -> {
                    Log.d(TAG, "handleStateResponse:${it.message}")
                }
                else -> {
                }
            }
        }
    }

    private fun handleStateError(errorEvent: Event<StateError>) {
        errorEvent.getContentIfNotHandled()?.let {
            when (it.response.responseType) {
                is ResponseType.Dialog -> displayErrorDialog(it.response.message)
                is ResponseType.Toast -> it.response.message?.let { msg ->
                    displayToast(msg)
                }
                is ResponseType.None -> {
                    Log.e(TAG, "handleStateError:${it.response.message}")
                }
                else -> {
                }
            }
        }
    }

    abstract fun displayProgressBar(boolean: Boolean)

    override fun isStoragePermissionGranted(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ), Constants.PERMISSIONS_REQUEST_READ_STORAGE
            )
            return false

        } else {
            return true
        }
    }
}