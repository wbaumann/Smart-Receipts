package co.smartreceipts.android.model.impl

import android.content.Context
import co.smartreceipts.android.model.*
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.sync.model.SyncState
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.sql.Date
import java.util.*

/**
 * A mostly immutable implementation of the [Receipt] interface that
 * serves as the default implementation.
 */
@Parcelize
class DefaultReceiptImpl constructor(
    override val id: Int,
    override val uuid: UUID,
    override val index: Int, // Tracks the index in the list (if specified)
    override val trip: Trip,
    override val file: File?,
    override val paymentMethod: PaymentMethod,
    override val name: String,
    override val category: Category,
    override val comment: String,
    override val price: Price,
    override val tax: Price,
    override val date: Date,
    override val timeZone: TimeZone,
    override val isReimbursable: Boolean,
    override val isFullPage: Boolean,
    override val isSelected: Boolean,
    override val source: Source,
    private val extraEditTextOne: String?,
    private val extraEditTextTwo: String?,
    private val extraEditTextThree: String?,
    override val syncState: SyncState,
    override val customOrderId: Long
    ) : Receipt {

    override val extraEditText1: String? = if (DatabaseHelper.NO_DATA == extraEditTextOne) null else extraEditTextOne
    override val extraEditText2: String? = if (DatabaseHelper.NO_DATA == extraEditTextTwo) null else extraEditTextTwo
    override val extraEditText3: String? = if (DatabaseHelper.NO_DATA == extraEditTextThree) null else extraEditTextThree

    override val fileName: String
        get() = file?.name ?: ""

    override val fileLastModifiedTime: Long
        get() = file?.lastModified() ?: -1

    override val filePath: String
        get() = file?.absolutePath ?: ""

    override fun hasImage(): Boolean {
        return file?.name?.run { endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") } ?: false
    }

    override fun hasPDF(): Boolean {
        return file?.name?.endsWith(".pdf") ?: false
    }

    override fun getFormattedDate(context: Context, separator: String): String {
        return ModelUtils.getFormattedDate(date, timeZone, context, separator)
    }

    override fun hasExtraEditText1(): Boolean = extraEditText1 != null

    override fun hasExtraEditText2(): Boolean = extraEditText2 != null

    override fun hasExtraEditText3(): Boolean = extraEditText3 != null

    override fun toString(): String {
        return "DefaultReceiptImpl{" +
                "id=" + id +
                ", uuid='" + uuid.toString() +
                ", name='" + name + '\''.toString() +
                ", trip=" + trip.name +
                ", paymentMethod=" + paymentMethod +
                ", index=" + index +
                ", comment='" + comment + '\''.toString() +
                ", category=" + category +
                ", price=" + price.currencyFormattedPrice +
                ", tax=" + tax +
                ", date=" + date +
                ", timeZone=" + timeZone.id +
                ", isReimbursable=" + isReimbursable +
                ", isFullPage=" + isFullPage +
                ", source=" + source +
                ", extraEditText1='" + extraEditText1 + '\''.toString() +
                ", extraEditText2='" + extraEditText2 + '\''.toString() +
                ", extraEditText3='" + extraEditText3 + '\''.toString() +
                ", isSelected=" + isSelected +
                ", file=" + file +
                ", fileLastModifiedTime=" + fileLastModifiedTime +
                ", customOrderId=" + customOrderId +
                '}'.toString()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is DefaultReceiptImpl) return false

        val that = o as DefaultReceiptImpl?

        if (id != that!!.id) return false
        if (uuid != that.uuid) return false
        if (isReimbursable != that.isReimbursable) return false
        if (isFullPage != that.isFullPage) return false
        if (trip != that.trip) return false
        if (paymentMethod != that.paymentMethod) return false
        if (index != that.index) return false
        if (name != that.name) return false
        if (comment != that.comment) return false
        if (category != that.category) return false
        if (price != that.price) return false
        if (tax != that.tax) return false
        if (date != that.date) return false
        if (timeZone != that.timeZone) return false
        if (if (extraEditText1 != null) extraEditText1 != that.extraEditText1 else that.extraEditText1 != null)
            return false
        if (if (extraEditText2 != null) extraEditText2 != that.extraEditText2 else that.extraEditText2 != null)
            return false
        if (if (extraEditText3 != null) extraEditText3 != that.extraEditText3 else that.extraEditText3 != null)
            return false
        if (fileLastModifiedTime != that.fileLastModifiedTime) return false
        if (customOrderId != that.customOrderId) return false
        return if (file != null) file == that.file else that.file == null

    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + uuid.hashCode()
        result = 31 * result + trip.hashCode()
        result = 31 * result + paymentMethod.hashCode()
        result = 31 * result + index
        result = 31 * result + name.hashCode()
        result = 31 * result + comment.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + tax.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + timeZone.hashCode()
        result = 31 * result + if (isReimbursable) 1 else 0
        result = 31 * result + if (isFullPage) 1 else 0
        result = 31 * result + (extraEditText1?.hashCode() ?: 0)
        result = 31 * result + (extraEditText2?.hashCode() ?: 0)
        result = 31 * result + (extraEditText3?.hashCode() ?: 0)
        result = 31 * result + if (file != null) file.hashCode() else 0
        result = 31 * result + fileLastModifiedTime.toInt()
        result = 31 * result + (customOrderId xor customOrderId.ushr(32)).toInt()
        return result
    }

    override fun compareTo(receipt: Receipt): Int {
        return if (customOrderId == receipt.customOrderId) {
            receipt.date.compareTo(date)
        } else {
            -java.lang.Long.compare(customOrderId, receipt.customOrderId)
        }
    }
}