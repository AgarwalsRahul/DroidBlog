package com.rahul.openapi.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.rahul.openapi.ui.DataState
import com.rahul.openapi.ui.Response
import com.rahul.openapi.ui.ResponseType
import com.rahul.openapi.util.Constants.Companion.NETWORK_TIMEOUT
import com.rahul.openapi.util.Constants.Companion.TESTING_CACHE_DELAY
import com.rahul.openapi.util.Constants.Companion.TESTING_NETWORK_DELAY
import com.rahul.openapi.util.ErrorHandling
import com.rahul.openapi.util.GenericApiResponse
import com.rahul.openapi.util.GenericApiResponse.*
import kotlinx.coroutines.*

abstract class NetworkBoundResource<ResponseObject, CacheObject, ViewStateType>(
    isNetworkAvailable: Boolean,
    isNetworkRequest: Boolean,// is this a network Request
    shouldLoadFromCache: Boolean, // data should be retrieve from cache before network request
    shouldCancelIfNoInternet: Boolean,
) {
    private val TAG = "AppDebug"

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(isLoading = true, cachedData = null))
        if (shouldLoadFromCache) {
            val dbSource = loadFromCache()
            result.addSource(dbSource) {
                result.removeSource(dbSource)
                setValue(DataState.loading(false, it))
            }
        }
        if (isNetworkRequest) {
            if (isNetworkAvailable) {
                performNetworkRequest()
            } else {
                if (shouldCancelIfNoInternet) {
                    onErrorReturn(
                        ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET,
                        ResponseType.Dialog()
                    )
                } else {
                    doCacheRequest()
                }
            }
        } else {
            doCacheRequest()
        }
    }

    private fun doCacheRequest() {
        coroutineScope.launch {
            delay(TESTING_CACHE_DELAY)
            // View data from cache only and return
            createCacheRequestAndReturn()
        }
    }

    private fun performNetworkRequest() {
        coroutineScope.launch {
            delay(TESTING_NETWORK_DELAY)

            withContext(Dispatchers.Main) {
                val apiResponse = createCall()
                result.addSource(apiResponse) {
                    result.removeSource(apiResponse)
                    coroutineScope.launch {
                        handleNetworkCall(it)
                    }
                }
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            delay(NETWORK_TIMEOUT)
            if (!job.isCompleted) {
                Log.e(TAG, "NetworkBoundResource: NETWORK TIMEOUT")
                job.cancel(CancellationException(ErrorHandling.UNABLE_TO_RESOLVE_HOST))
            }
        }
    }


    private suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>?) {
        when (response) {
            is ApiSuccessResponse -> handleApiSuccessResponse(response)
            is ApiErrorResponse -> {
                Log.e(TAG, "NetworkBoundResource: ${response.errorMessage}")
                onErrorReturn(response.errorMessage, responseType = ResponseType.Toast())
            }
            is ApiEmptyResponse -> {
                Log.e(TAG, "NetworkBoundResource: Request returned NOTHING HTTP 204}")
                onErrorReturn(ErrorHandling.ERROR_UNKNOWN, responseType = ResponseType.Toast())

            }
        }
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Dispatchers.Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun onErrorReturn(errorMessage: String?, responseType: ResponseType) {
        var msg = errorMessage
        var response_type = responseType
        if (msg == null) {
            msg = ErrorHandling.ERROR_UNKNOWN
        } else if (ErrorHandling.isNetworkError(msg)) {
            msg = ErrorHandling.ERROR_CHECK_NETWORK_CONNECTION
            response_type = ResponseType.Toast()
        }
        onCompleteJob(DataState.error(response = Response(msg, response_type)))
    }

    private fun initNewJob(): Job {
        Log.d(TAG, "initNewJob: Called..")
        job = Job()
        job.invokeOnCompletion(
            onCancelling = true,
            invokeImmediately = true,
            handler = object : CompletionHandler {
                override fun invoke(cause: Throwable?) {
                    if (job.isCancelled) {
                        Log.e(TAG, "NetworkBoundResource: Job has been cancelled .. ")
                        cause?.let {
                            it.message?.let { msg ->
                                onErrorReturn(msg, responseType = ResponseType.Toast())
                            } ?: onErrorReturn(
                                ErrorHandling.ERROR_UNKNOWN,
                                responseType = ResponseType.Toast()
                            )
                        }
                    } else if (job.isCompleted) {
                        Log.d(TAG, "NetworkBoundResource: Job has been completed ...")
                        // Already been handled
                    }
                }

            })
        coroutineScope = CoroutineScope(Dispatchers.IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)

    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>

    abstract fun loadFromCache(): LiveData<ViewStateType>

    abstract suspend fun updateLocalDb(cacheObject: CacheObject?)

    abstract suspend fun createCacheRequestAndReturn()

    abstract fun setJob(job: Job)

}