package co.smartreceipts.android.model.impl

import android.content.Context
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.android.sync.model.SyncState
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.sql.Date
import java.util.*

/**
 * An immutable [Distance] implementation to track distance.
 */
@Parcelize
class ImmutableDistanceImpl(
    override val id: Int,
    override val uuid: UUID,
    override val trip: Trip,
    override val location: String,
    override val distance: BigDecimal,
    override val rate: BigDecimal,
    override val price: Price,
    override val date: Date,
    override val timeZone: TimeZone,
    override val comment: String,
    override val syncState: SyncState
) : Distance {

    override val decimalFormattedDistance: String
        get() = ModelUtils.getDecimalFormattedValue(distance)

    override val decimalFormattedRate: String
        get() = ModelUtils.getDecimalFormattedValue(rate, Distance.RATE_PRECISION)

    override val currencyFormattedRate: String
        get() {
            val precision =
                if (decimalFormattedRate.endsWith("0")) Price.DEFAULT_DECIMAL_PRECISION else Distance.RATE_PRECISION
            return ModelUtils.getCurrencyFormattedValue(rate, price.currency, precision)
        }


    override fun getFormattedDate(context: Context, separator: String): String =
        ModelUtils.getFormattedDate(date, timeZone, context, separator)

    override fun toString(): String {
        return "Distance [uuid=$uuid, mLocation=$location, mDistance=$distance, mDate=$date, mTimezone=$timeZone, mRate=$rate, mPrice= $price, mComment=$comment]"
    }

    override fun compareTo(distance: Distance): Int {
        return distance.date.compareTo(date)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImmutableDistanceImpl

        if (id != other.id) return false
        if (uuid != other.uuid) return false
        if (trip != other.trip) return false
        if (location != other.location) return false
        if (distance != other.distance) return false
        if (rate != other.rate) return false
        if (price != other.price) return false
        if (date != other.date) return false
        if (timeZone != other.timeZone) return false
        if (comment != other.comment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + uuid.hashCode()
        result = 31 * result + trip.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + distance.hashCode()
        result = 31 * result + rate.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + timeZone.hashCode()
        result = 31 * result + comment.hashCode()
        return result
    }

}
