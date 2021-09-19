package com.rahul.openapi.ui

data class Loading(val isLoading: Boolean)
data class Data<T>(val data: Event<T>?, val response: Event<Response>?)
data class StateError(val response: Response)

data class Response(val message: String?, val responseType: ResponseType)

sealed class ResponseType {
    class Toast : ResponseType()
    class Dialog : ResponseType()
    class None : ResponseType()
}


/*
    Used as a wrapper class for the data exposed via livedata
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled: Boolean = false
        private set // Allow external read but not write


    // Return the content and prevent it's use again
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /*
     Return the content, even if it has already been handled
     */
    fun peekContent(): T = content
    override fun toString(): String {
        return "Event(content=$content, hasBeenHandled=$hasBeenHandled)"
    }


    companion object {
        //we don't want an event if the data is null
        fun <T> dataEvent(data: T?): Event<T>? {
            data?.let {
                return Event(it)
            }
            return null
        }

        fun <T> responseEvent(response: Response?): Event<Response>? {
            response?.let {
                return Event(it)
            }
            return null
        }
    }
}