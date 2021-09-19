package com.rahul.openapi.session

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.persistence.AuthTokenDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {

    companion object {
        private const val TAG = "AppDebug"
    }

    private val _cachedToken = MutableLiveData<AuthToken>()
    val cachedToken: LiveData<AuthToken>
        get() = _cachedToken

    fun login(newValue: AuthToken) {
        setValue(newValue)
    }

    fun logout() {
        Log.d(TAG, "LOGOUT....")

        GlobalScope.launch(Dispatchers.IO) {
            var errorMessage: String? = null
            try {
                _cachedToken.value!!.account_pk?.let {
                    authTokenDao.nullifyToken(it)
                }
            } catch (e: CancellationException) {
                Log.e(TAG, "LOGOUT: ${e.message}")
                errorMessage = e.message
            } catch (e: Exception) {
                Log.e(TAG, "LOGOUT: ${e.message}")
                errorMessage = errorMessage + "\n" + e.message
            } finally {
                errorMessage?.let {
                    Log.e(TAG, "LOGOUT: $it")
                }
                Log.d(TAG, "LOGOUT: Finally....")
                setValue(null)
            }
        }
    }

    fun setValue(newValue: AuthToken?) {
        GlobalScope.launch(Dispatchers.Main) {
            if (_cachedToken.value != newValue) {
                _cachedToken.value = newValue
            }
        }
    }

    fun checkNetworkConnection(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val network = cm.activeNetwork;

                val networkCapabilities = cm.getNetworkCapabilities(network);

                val isInternetSuspended =
                    !networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)!!;
                (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        && !isInternetSuspended);
            }else{
                cm.activeNetworkInfo!!.isConnected
            }

        } catch (e: Exception) {
            Log.e(TAG,"IsNetworkConnected : ${e.message}")
        }
        return false
    }
}