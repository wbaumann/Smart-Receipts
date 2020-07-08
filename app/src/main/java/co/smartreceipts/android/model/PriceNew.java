package co.smartreceipts.android.model;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.gson.ExchangeRate;

/**
 * Defines a contract from which we can track the price value
 */
public interface PriceNew extends Parcelable {

    // TODO: 09.07.2020 remove old constants and clean up
    /**
     * The default decimal precisions for prices (ie three decimal points) like "$2.225" instead of "$2.23"
     */
    int DEFAULT_DECIMAL_PRECISION = 3;

    /**
     * The decimal precision for price totals (ie two decimal points) like "$2.22" instead of "$2.22222"
     */
    int TOTAL_DECIMAL_PRECISION = 2;

    /**
     * Defines the default precision rate that we use for rounding off our multiplied values (in
     * conjunction with the exchange rates)
     */
    int ROUNDING_PRECISION = 5;

    /**
     * Gets the float representation of this price
     *
     * @return the float primitive, which represents the total price of this receipt
     */
    float getPriceAsFloat();

    /**
     * Gets the {@link BigDecimal} representation of this price
     *
     * @return the {@link BigDecimal} representation of this price
     */
    @NonNull
    BigDecimal getPrice();

    @NonNull
    BigMoney getMoney();

    /**
     * A "decimal-formatted" price, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2"
     *
     * @return the decimal formatted price {@link String}
     */
    @NonNull
    String getDecimalFormattedPrice();

    /**
     * The "currency-formatted" price, which would appear as "$25.20" or "$25,20" as determined by the user's locale
     *
     * @return - the currency formatted price {@link String}
     */
    @NonNull
    String getCurrencyFormattedPrice();

    /**
     * The "currency-code-formatted" price, which would appear as "USD25.20" or "USD25,20" as determined by the user's locale
     *
     * @return - the currency formatted price {@link String}
     */
    @NonNull
    String getCurrencyCodeFormattedPrice();


    @NonNull
    CurrencyUnit getCurrency();

    /**
     * Gets the currency code representation for this price or {@link PriceCurrency#MISSING_CURRENCY}
     * if it cannot be found
     *
     * @return the currency code {@link String} for this price
     */
    @NonNull
    String getCurrencyCode();

    boolean isSingleCurrency();

    /**
     * Gets the exchange rate associated with this particular price object, which we can use to attempt to convert this
     * price from one currency to another
     *
     * @return the {@link ExchangeRate}
     */
    @NonNull
    ExchangeRate getExchangeRate();

}
