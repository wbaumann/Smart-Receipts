package co.smartreceipts.android.licenses

import android.content.Context
import android.content.Intent
import co.smartreceipts.core.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class NoOpLicensesNavigator @Inject constructor(): LicensesNavigator {

    override fun getLicensesActivityIntent(context: Context): Intent? = null
}