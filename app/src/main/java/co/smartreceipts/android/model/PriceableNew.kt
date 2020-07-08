package co.smartreceipts.android.model

/**
 * A simple interface for any class that has pricing information via the [PriceNew]
 * interface
 */
interface PriceableNew {

    /**
     * Gets the price for this particular item
     *
     * @return the [PriceNew]
     */
    val price: PriceNew
}
