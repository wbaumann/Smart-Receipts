package co.smartreceipts.android.workers.reports

import android.content.Context
import android.content.res.Configuration
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import java.util.*
import javax.inject.Inject

@ApplicationScope
class ReportResourcesManager @Inject
constructor(private val context: Context, private val preferenceManager: UserPreferenceManager) {

    fun getLocalizedContext(): Context {
        val desiredLocale =
            Locale(preferenceManager.get(UserPreference.ReportOutput.PreferredReportLanguage))
        val conf = Configuration(context.resources.configuration)
        conf.setLocale(desiredLocale)

        return context.createConfigurationContext(conf)
    }
}
