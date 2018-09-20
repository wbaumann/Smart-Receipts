package co.smartreceipts.android.model

import android.content.Context
import android.os.Parcelable
import co.smartreceipts.android.sync.model.Syncable
import java.io.File
import java.sql.Date
import java.util.*

interface Receipt : Parcelable, Priceable, Draggable<Receipt>, Syncable {

    /**
     * Gets the parent trip for this receipt. This can only be null if it's detached from a [Trip]
     * (e.g. if it's a converted distance).
     *
     * @return - the parent [Trip]
     */
    val trip: Trip

    /**
     * Gets the payment method associated with this receipt item.
     *
     * @return the [co.smartreceipts.android.model.PaymentMethod] associated with this receipt item.
     */
    val paymentMethod: PaymentMethod

    /**
     * Gets the name of this receipt. This should never be `null`.
     *
     * @return the name of this receipt as a [String].
     */
    val name: String

    /**
     * Gets the file attached to this receipt.
     *
     * @return the Image [File] or `null` if none is presentFirstTimeInformation
     */
    val file: File?

    /**
     * Gets the absolute path of this Receipt's file from [.getFile].
     *
     * @return a representation of the file path via [.getFile] and [File.getAbsolutePath].
     */
    val filePath: String

    /**
     * Gets the name of this Receipt's file from [.getFile].
     *
     * @return a representation of the file name via [.getFile] and [File.getName].
     */
    val fileName: String

    /**
     * Java uses immutable [File], so when we rename our files as part of a receipt update, we might rename it
     * to the same file name as the old receipt. By tracking the last update time as well, we can determine if this file
     * was updated between two "like" receipts
     *
     * @return the last updated time or `-1` if we don't have a file
     */
    val fileLastModifiedTime: Long

    /**
     * Gets the source from which this receipt was built for debugging purposes
     *
     * @return the [Source]
     */
    val source: Source

    /**
     * Gets the category to which this receipt is attached
     *
     * @return the [Category] this receipt uses
     */
    val category: Category

    /**
     * Gets the user defined comment for this receipt
     *
     * @return - the current comment as a [String]
     */
    val comment: String


    /**
     * Gets the tax associated with this receipt
     *
     * @return the [Price] for the tax
     */
    val tax: Price


    /**
     * Returns the date during which this receipt was taken
     *
     * @return the [Date] this receipt was captured
     */
    val date: Date

    /**
     * Gets the time zone in which the date was set
     *
     * @return - the [TimeZone] for the date
     */
    val timeZone: TimeZone

    /**
     * Checks if the receipt was marked as Reimbursable (i.e. counting towards the total) or not
     *
     * @return `true` if it's Reimbursable, `false` otherwise
     */
    val isReimbursable: Boolean

    /**
     * Checks if this receipt should be printed as a full page in the PDF report
     *
     * @return `true` if it's printed as a full page, `false` otherwise
     */
    val isFullPage: Boolean

    /**
     * Checks if this receipt is currently selected or not
     *
     * @return `true` if it's currently selected. `false` otherwise
     */
    val isSelected: Boolean

    /**
     * Returns the "index" of this receipt relative to others. If this was the second earliest receipt, it would appear
     * as a receipt of index 2.
     *
     * @return the index of this receipt
     */
    val index: Int

    /**
     * Returns the user defined string for the 1st "extra" field
     *
     * @return the [String] for the 1st custom field or `null` if not set
     */
    val extraEditText1: String?

    /**
     * Returns the user defined string for the 2nd "extra" field
     *
     * @return the [String] for the 2nd custom field or `null` if not set
     */
    val extraEditText2: String?

    /**
     * Returns the user defined string for the 3rd "extra" field
     *
     * @return the [String] for the 3rd custom field or `null` if not set
     */
    val extraEditText3: String?

    /**
     * Checks if this receipt is connected to an image file
     *
     * @return `true` if it has an image file, `false` otherwise
     */
    fun hasImage(): Boolean

    /**
     * Checks if this receipt is connected to an PDF file
     *
     * @return `true` if it has a PDF file, `false` otherwise
     */
    fun hasPDF(): Boolean

    /**
     * Gets a formatted version of the date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param context   - the current [Context]
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for this receipt
     */
    fun getFormattedDate(context: Context, separator: String): String

    /**
     * Checks if we have a 1st "extra" field
     *
     * @return `true` if we have a 1st "extra" field or `false` if not
     */
    fun hasExtraEditText1(): Boolean

    /**
     * Checks if we have a 2nd "extra" field
     *
     * @return `true` if we have a 2nd "extra" field or `false` if not
     */
    fun hasExtraEditText2(): Boolean

    /**
     * Checks if we have a 3rd "extra" field
     *
     * @return `true` if we have a 3rd "extra" field or `false` if not
     */
    fun hasExtraEditText3(): Boolean

    companion object {

        val PARCEL_KEY: String = Receipt::class.java.name
    }

}