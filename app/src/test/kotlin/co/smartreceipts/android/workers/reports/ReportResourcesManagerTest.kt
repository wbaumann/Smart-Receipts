package co.smartreceipts.android.workers.reports

import android.support.v4.os.ConfigurationCompat
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import wb.android.flex.Flex
import java.util.*

@RunWith(RobolectricTestRunner::class)
//@Config(minSdk = N)
class ReportResourcesManagerTest {

    lateinit var reportResourcesManager: ReportResourcesManager

    @Mock
    lateinit var flex: Flex

    @Mock
    lateinit var preferences: UserPreferenceManager

    @Before
//    @Config(minSdk = Build.VERSION_CODES.N)
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        val context = RuntimeEnvironment.systemContext//.application.applicationContext

        `when`(preferences.get(UserPreference.ReportOutput.PreferredReportLanguage)).thenReturn("en")
//        `when`(flex.getString())

        reportResourcesManager = ReportResourcesManager(context, preferences, flex)
    }

    @Test
    fun getDefaultLocalizedContext() {

        val locale =
            ConfigurationCompat.getLocales(reportResourcesManager.getLocalizedContext().resources.configuration)
                .get(0)
        assertEquals(Locale("en"), locale)
    }
}