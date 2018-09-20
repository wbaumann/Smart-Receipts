package co.smartreceipts.android.model.impl

import android.content.res.Resources
import co.smartreceipts.android.model.PaymentMethod
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.sync.model.impl.DefaultSyncState
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * An immutable implementation of [PaymentMethod].
 *
 * @author Will Baumann
 */
@Parcelize
class ImmutablePaymentMethodImpl @JvmOverloads constructor (
    override val id: Int,
    override val uuid: UUID,
    override val method: String,
    override val syncState: SyncState = DefaultSyncState(),
    override val customOrderId: Long = 0

) : PaymentMethod {

    override fun toString() = method

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ImmutablePaymentMethodImpl) return false

        val that = o as ImmutablePaymentMethodImpl?

        if (id != that!!.id) return false
        if (uuid != that.uuid) return false
        return if (customOrderId != that.customOrderId) false else method == that.method
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + uuid.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + (customOrderId xor customOrderId.ushr(32)).toInt()
        return result
    }

    override fun compareTo(paymentMethod: PaymentMethod): Int {
        return customOrderId.compareTo(paymentMethod.customOrderId)
    }

    companion object {
        val NONE = PaymentMethodBuilderFactory().setMethod(Resources.getSystem().getString(android.R.string.untitled)).build()
    }
}
