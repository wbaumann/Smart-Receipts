package co.smartreceipts.android.model

import android.os.Parcelable

import co.smartreceipts.android.sync.model.Syncable

/**
 * This interface establishes the contract for encapsulating the behavior of different payment models.
 *
 * @author Will Baumann
 */
interface PaymentMethod : Parcelable, Syncable, Draggable<PaymentMethod> {

    /**
     * The actual payment method that the user specified
     */
    val method: String

}
