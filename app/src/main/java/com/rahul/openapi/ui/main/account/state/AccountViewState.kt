package com.rahul.openapi.ui.main.account.state

import android.os.Parcelable
import com.rahul.openapi.models.AccountProperties
import kotlinx.android.parcel.Parcelize


const val ACCOUNT_VIEW_STATE_BUNDLE_KEY = "AccountViewState"

@Parcelize
class AccountViewState(var accountProperties: AccountProperties? = null):Parcelable {
}