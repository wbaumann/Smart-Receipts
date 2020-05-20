package co.smartreceipts.android.test.espresso

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.SmartReceiptsActivity
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReceiptPhotosPDFTest {

    @get:Rule
    val mIntentsRule = IntentsTestRule(SmartReceiptsActivity::class.java)

    private var uriList: ArrayList<Uri>? = null

    @Before
    fun setUp() {
        // Put the drawable in a bundle
        val bundle = Bundle()
        bundle.putParcelable("instrumented_test", BitmapFactory.decodeResource(
                mIntentsRule.activity.resources, R.drawable.ic_launcher))

        // Create the Intent that will include the bundle.
        val resultData = Intent()
        resultData.putExtras(bundle)

        // Create the ActivityResult with the Intent.
        val result = ActivityResult(Activity.RESULT_OK, resultData)

        // Dismiss the share report intent
        intending(not(isInternal())).respondWith(ActivityResult(Activity.RESULT_OK, null))

        // Stub the returned photo data intents
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result)
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)

        val authority = String.format(Locale.US, "%s.fileprovider", InstrumentationRegistry.getInstrumentation().targetContext.packageName)
        val uri = Uri.parse("content://$authority/public-files-path/Photos Test/Photos Test.pdf".replace(" ", "%20"))
        uriList = arrayListOf(uri)
    }

    @Test
    fun createTripAddCameraAndGalleryPhotosGenerate() {
        // Click on the "new report" button
        onView(withId(R.id.trip_action_new)).perform(click())

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_name)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_start)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_end)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_currency)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_comment)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_cost_center)).check(matches(not(isDisplayed())))

        // Create a trip, entitled "Photos Test"
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText("Photos Test"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        Thread.sleep(TimeUnit.SECONDS.toMillis(1)) // Wait a second to ensure that everything loaded

        // Open the fab menu
        onView(allOf(withParent(withId(R.id.fab_menu)), withClassName(endsWith("ImageView")), isDisplayed())).perform(click())

        // Click on "camera" button
        onView(withId(R.id.receipt_action_camera)).perform(click())

        Thread.sleep(TimeUnit.SECONDS.toMillis(1)) // Wait a second to ensure that everything loaded

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).perform(replaceText("Camera Receipt"))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).perform(replaceText("12.34"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Open the fab menu
        onView(allOf(withParent(withId(R.id.fab_menu)), withClassName(endsWith("ImageView")), isDisplayed())).perform(click())

        // Click on "import" button
        onView(withId(R.id.receipt_action_import)).perform(click())

        // Click on the IMAGE option
        onView(withText("Image")).perform(click())

        Thread.sleep(TimeUnit.SECONDS.toMillis(1)) // Wait a second to ensure that everything loaded

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).perform(replaceText("Gallery Receipt"))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).perform(replaceText("12.34"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        Thread.sleep(TimeUnit.SECONDS.toMillis(1)) // Wait a second to ensure that everything loaded

        // Go to generate screen
        onView(withText("GENERATE")).perform(click())

        // Check the box for Full PDF Report
        onView(withId(R.id.dialog_email_checkbox_pdf_full)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

        Thread.sleep(TimeUnit.SECONDS.toMillis(5)) // give app time to generate files and display intent chooser

        // Verify the intent chooser with a PDF report was displayed
        intended(allOf(hasAction(Intent.ACTION_CHOOSER), hasExtra(`is`(Intent.EXTRA_INTENT), allOf(hasAction(Intent.ACTION_SEND_MULTIPLE), hasType("application/pdf"), hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), hasExtra(Intent.EXTRA_STREAM, uriList)))))
    }

}
