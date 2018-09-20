package co.smartreceipts.android.model

import android.content.Context
import android.os.Parcelable
import co.smartreceipts.android.currency.PriceCurrency
import co.smartreceipts.android.sync.model.Syncable
import java.io.File
import java.sql.Date
import java.util.*

interface Trip : Parcelable, Priceable, Comparable<Trip>, Syncable {

    /**
     * As the price of a trip exists as a function of it's receipt children (and not itself), [Price] must be var
     */
    override var price: Price

    /**
     * The directory in which all this trip's images are stored
     */
    val directory: File

    /**
     * The [Date] upon which this trip began
     */
    val startDate: Date

    /**
     * The [TimeZone] in which the start date was set
     */
    val startTimeZone: TimeZone

    /**
     * The [Date] upon which this trip will end
     */
    val endDate: Date

    /**
     * The [TimeZone] in which the end date was set
     */
    val endTimeZone: TimeZone

    /**
     * The daily sub-total [Price] (i.e. all expenditures that occurred today) for this trip
     */
    /**
     * As the daily sub-total of a trip exists as a function of it's receipt children (and not itself), this method can
     * be used to update the sub-total representation
     *
     * @param dailySubTotal - the new daily sub-total [Price] to use
     */
    var dailySubTotal: Price

    /**
     * The [PriceCurrency] which this trip is tracked in
     */
    val tripCurrency: PriceCurrency

    /**
     * The user defined comment [String] for this trip
     */
    val comment: String

    /**
     * The cost center for this particular trip
     */
    val costCenter: String

    /**
     * The [Source] from which this trip was built for debugging purposes
     */
    val source: Source

    /**
     * The name of this trip (this will be the name of [.getDirectory]
     */
    val name: String

    /**
     * The absolute path of this Trip's directory from [.getDirectory] and [File.getAbsolutePath]
     */
    val directoryPath: String

    /**
     * The default currency code representation for this trip or [PriceCurrency.MISSING_CURRENCY]
     * if it cannot be found
     */
    val defaultCurrencyCode: String

    /**
     * Gets a formatted version of the start date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param context   - the current [Context]
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for the start date
     */
    fun getFormattedStartDate(context: Context, separator: String): String

    /**
     * Gets a formatted version of the end date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param context   - the current [Context]
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for the end date
     */
    fun getFormattedEndDate(context: Context, separator: String): String

    /**
     * Tests if a particular date is included with the bounds of this particular trip. When performing the test, it uses
     * the local time zone for the date, and the defined time zones for the start and end date bounds. The start date
     * time is assumed to occur at 00:01 of the start day and the end date is assumed to occur at 23:59 of the end day.
     * The reasoning behind this is to ensure that it appears properly from a UI perspective. Since the initial date
     * only shows the day itself, it may include an arbitrary time that is never shown to the user. Setting the time
     * aspect manually accounts for this. This returns false if the date is null.
     *
     * @param date - the [Date] to test
     * @return `true` if it is contained within. `false` otherwise
     */
    fun isDateInsideTripBounds(date: Date?): Boolean


    companion object {

        val PARCEL_KEY: String = Trip::class.java.name
    }

}
