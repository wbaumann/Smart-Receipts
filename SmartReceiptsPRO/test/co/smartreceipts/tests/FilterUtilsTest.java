package co.smartreceipts.tests;

import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.filters.FilterUtils;

@Config(emulateSdk = 18, manifest = "../SmartReceiptsPRO/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class FilterUtilsTest {

	// These scenario is using same time representation
	// with different timezone for all assertion (let's say 09:00)
	private final long millis = new java.util.Date().getTime();
	private final Date date = new Date(millis);

	private final TimeZone tz1 = TimeZone.getTimeZone("GMT+01:00");
	private final TimeZone tz2 = TimeZone.getTimeZone("GMT+02:00");
	private final TimeZone tz3 = TimeZone.getTimeZone("GMT+03:00");

	@Test
	public void isOnOrAfterTimezoneHandlingTest() {
		// combining timezone
		assertTrue(FilterUtils.isOnOrAfter(date, tz1, date, tz2));
		assertTrue(FilterUtils.isOnOrAfter(date, tz2, date, tz3));
		assertTrue(FilterUtils.isOnOrAfter(date, tz1, date, tz3));

		// same timezone
		assertTrue(FilterUtils.isOnOrAfter(date, tz1, date, tz1));
		assertTrue(FilterUtils.isOnOrAfter(date, tz2, date, tz2));
		assertTrue(FilterUtils.isOnOrAfter(date, tz3, date, tz3));
	}
	
	@Test
	public void isOnOrBeforeTimezoneHandlingTest() {
		// combining timezone
		assertTrue(FilterUtils.isOnOrBefore(date, tz2, date, tz1));
		assertTrue(FilterUtils.isOnOrBefore(date, tz3, date, tz2));
		assertTrue(FilterUtils.isOnOrBefore(date, tz3, date, tz1));

		// same timezone
		assertTrue(FilterUtils.isOnOrBefore(date, tz1, date, tz1));
		assertTrue(FilterUtils.isOnOrBefore(date, tz2, date, tz2));
		assertTrue(FilterUtils.isOnOrBefore(date, tz3, date, tz3));
	}
}
