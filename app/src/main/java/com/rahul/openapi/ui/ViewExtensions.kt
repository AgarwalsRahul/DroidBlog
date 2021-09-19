package com.rahul.openapi.ui

import android.app.Activity
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.rahul.openapi.R


fun Activity.displayToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.displaySuccessDialog(message: String?) {
    MaterialDialog(this).show {
        title(R.string.text_success)
        message(text = message)
        positiveButton(R.string.text_ok)
    }
}

fun Activity.displayErrorDialog(message: String?) {
    MaterialDialog(this).show {
        title(R.string.text_error)
        message(text = message)
        positiveButton(R.string.text_ok)
    }

}

fun Activity.displayInfoDialog(msg: String?) {
    MaterialDialog(this).show {
        title(R.string.text_info)
        message(text = msg)
        positiveButton(R.string.text_ok)
    }
}

fun Activity.areYouSureDialog(msg: String?, callback: AreyouSureCallback) {
    MaterialDialog(this).show {
        title(R.string.are_you_sure)
        message(text = msg)
        positiveButton(R.string.text_yes) {
            callback.proceed()
        }

        negativeButton(R.string.text_cancel) {
            callback.cancel()
        }
    }
}

interface AreyouSureCallback {
    fun cancel()

    fun proceed()
}