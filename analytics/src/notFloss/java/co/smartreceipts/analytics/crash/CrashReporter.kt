package co.smartreceipts.analytics.crash

import co.smartreceipts.analytics.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class CrashReporter @Inject constructor() : CrashReporterInterface {

    /**
     * Initializes our crash reporter to determine if we should track crashes or not
     */
    override fun initialize(isCrashTrackingEnabled: Boolean) {
        // Set up Crashlytics, disabling it when the user has elected to disable the functionality
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(isCrashTrackingEnabled)
        } else {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }
}
