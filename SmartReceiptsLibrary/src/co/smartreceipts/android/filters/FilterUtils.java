package co.smartreceipts.android.filters;

import java.sql.Date;
import java.util.TimeZone;

public class FilterUtils {

	public static boolean isOnOrBefore(Date dt1, TimeZone tz1, Date dt2,
			TimeZone tz2) {
		// TODO still not sure about dealing with TimeZone
		return dt1.equals(dt2) || dt1.before(dt2);
	}

	public static boolean isOnOrAfter(Date dt1, TimeZone tz1, Date dt2,
			TimeZone tz2) {
		// TODO still not sure about dealing with TimeZone
		return dt1.equals(dt2) || dt1.after(dt2);
	}
}
