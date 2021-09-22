package com.rahul.openapi.ui

data class UIMessage(
    val message: String,
    val uiMessageType: UIMessageType,
)

sealed class UIMessageType {

    object Toast : UIMessageType()

    object Dialog : UIMessageType()

    object None : UIMessageType()

    class AreYouSureDialog(val callback: AreYouSureCallback) : UIMessageType()
}