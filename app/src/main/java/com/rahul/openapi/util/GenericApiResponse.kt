package com.rahul.openapi.util

import android.util.Log
import retrofit2.Response

sealed class GenericApiResponse<T> {

    class ApiEmptyResponse<T> : GenericApiResponse<T>()

    data class ApiErrorResponse<T>(val errorMessage: String) : GenericApiResponse<T>()

    data class ApiSuccessResponse<T>(val body: T) : GenericApiResponse<T>()

    companion object{
        private const val TAG = "GenericApiResponse"

        fun <T> create(error:Throwable):ApiErrorResponse<T>{
            return ApiErrorResponse(errorMessage = error.message?: "Unknown error")
        }

        fun <T> create(response:Response<T>) : GenericApiResponse<T>{
            Log.d(TAG,"response: $response")
            Log.d(TAG,"raw: ${response.raw()}")
            Log.d(TAG,"headers: ${response.headers()}")
            Log.d(TAG,"message: ${response.message()}")
            if(response.isSuccessful){
                val body = response.body()
                return if (body==null || response.code()==204){
                    ApiEmptyResponse()
                }else if(response.code()==401){
                    ApiErrorResponse("401 Unauthorized. Token may be invalid.")
                }else{
                    ApiSuccessResponse(body)
                }
            }else{
                val msg = response.errorBody()?.string()
                val errorMsg = if(msg.isNullOrEmpty()){
                    response.message()
                }else{
                    msg
                }
                return ApiErrorResponse(errorMsg?:"Unknown Error")
            }
        }
    }
}