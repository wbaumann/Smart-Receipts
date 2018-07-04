package co.smartreceipts.android.workers.reports

import android.content.Context
import android.content.res.Configuration
import android.support.annotation.StringRes
import android.support.v4.os.ConfigurationCompat
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import wb.android.flex.Flex
import java.util.*
import javax.inject.Inject

@ApplicationScope
class ReportResourcesManager @Inject constructor(
    context: Context,
    private val preferenceManager: UserPreferenceManager,
    private val flex: Flex
) {

    private var localizedContext: Context

    init {
        val desiredLocale =
            Locale(preferenceManager.get(UserPreference.ReportOutput.PreferredReportLanguage))

        val conf = Configuration(context.resources.configuration)
        conf.setLocale(desiredLocale)

        localizedContext = context.createConfigurationContext(conf)

    }

    fun getLocalizedContext(): Context {
        val currentLocalizedContextLocale =
            ConfigurationCompat.getLocales(localizedContext.resources.configuration).get(0)

        val desiredLocale =
            Locale(preferenceManager.get(UserPreference.ReportOutput.PreferredReportLanguage))

        if (currentLocalizedContextLocale != desiredLocale) {
            val conf = Configuration(localizedContext.resources.configuration)
            conf.setLocale(desiredLocale)
            localizedContext = localizedContext.createConfigurationContext(conf)
        }

        return localizedContext

    }

    fun getFlexString(@StringRes resId: Int): String {
        return flex.getString(getLocalizedContext(), resId)
    }
}
