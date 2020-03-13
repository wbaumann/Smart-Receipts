package co.smartreceipts.android.licenses

import android.content.Context
import android.content.Intent
import co.smartreceipts.android.R
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import javax.inject.Inject

@ApplicationScope
class LicensesNavigatorImpl @Inject constructor() : LicensesNavigator {

    override fun getLicensesActivityIntent(context: Context): Intent? {

        OssLicensesMenuActivity.setActivityTitle(context.getString(R.string.pref_about_oss_title))
        return Intent(context, OssLicensesMenuActivity::class.java)
    }
}