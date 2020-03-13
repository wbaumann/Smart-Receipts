package co.smartreceipts.android.licenses

import android.content.Context
import android.content.Intent

interface LicensesNavigator {

    fun getLicensesActivityIntent(context: Context): Intent?
}