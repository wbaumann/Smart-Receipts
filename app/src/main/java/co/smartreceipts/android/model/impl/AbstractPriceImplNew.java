package co.smartreceipts.android.model.impl;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.PriceNew;

/**
 * Provides common methods that all {@link PriceNew} implementations use
 */
abstract class AbstractPriceImplNew implements PriceNew {

    protected static final float EPSILON = 1f / (Price.ROUNDING_PRECISION + 2f);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPriceImplNew)) return false;

        PriceNew that = (PriceNew) o;

        if (!getCurrency().equals(that.getCurrency())) {
            return false;
        }
        if (Math.abs(getPrice().floatValue() - that.getPrice().floatValue()) > EPSILON) {
            return false;
        }
        return getCurrencyFormattedPrice().equals(that.getCurrencyFormattedPrice());
    }

    @Override
    public int hashCode() {
        int result = getPrice().hashCode();
        result = 31 * result + getCurrency().hashCode();
        result = 31 * result + getCurrencyFormattedPrice().hashCode();
        return result;
    }
}
