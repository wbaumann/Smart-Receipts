package co.smartreceipts.android.filters;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class FilterUtils {

	public static boolean isOnOrBefore(Date dt1, TimeZone tz1, Date dt2, TimeZone tz2) {
		Calendar cal1 = Calendar.getInstance(tz1);
		cal1.setTime(dt1);

		Calendar cal2 = Calendar.getInstance(tz2);
		cal2.setTime(dt2);

		return cal1.getTimeInMillis() <= cal2.getTimeInMillis();
	}

	public static boolean isOnOrAfter(Date dt1, TimeZone tz1, Date dt2, TimeZone tz2) {
		Calendar cal1 = Calendar.getInstance(tz1);
		cal1.setTime(dt1);

		Calendar cal2 = Calendar.getInstance(tz2);
		cal2.setTime(dt2);

		return cal1.getTimeInMillis() >= cal2.getTimeInMillis();
	}
}
