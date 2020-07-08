package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import androidx.annotation.NonNull;

import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

import co.smartreceipts.android.model.gson.ExchangeRate;

/**
 * Defines an immutable implementation of the {@link co.smartreceipts.android.model.PriceNew} interface
 */
public final class SinglePriceImplNew extends AbstractPriceImplNew {


    private final Money money;
    private final ExchangeRate exchangeRate;
    private final String decimalFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyCodeFormattedPrice; // Note: We create/cache this as it's common, slower operation


    public SinglePriceImplNew(@NonNull BigDecimal price, @NonNull CurrencyUnit currency, @NonNull ExchangeRate exchangeRate) {
        this.money = Money.of(currency, price, RoundingMode.HALF_UP);
        this.exchangeRate = exchangeRate;


        // Pre-cache formatted values here
        this.decimalFormattedPrice = money.getAmount().toPlainString();
        this.currencyFormattedPrice = money.getCurrencyUnit().getSymbol().concat(money.getAmount().toPlainString());
        this.currencyCodeFormattedPrice = money.toString();
    }

    private SinglePriceImplNew(@NonNull Parcel in) {
        this.money = (Money) in.readSerializable();
        this.exchangeRate = (ExchangeRate) in.readSerializable();

        // Pre-cache formatted values here
        this.decimalFormattedPrice = money.getAmount().toPlainString();
        this.currencyFormattedPrice = money.getCurrencyUnit().getSymbol().concat(money.getAmount().toPlainString());
        this.currencyCodeFormattedPrice = money.toString();
    }

    // TODO: 29.06.2020 looks like this fun is not needed actually
    @Override
    public float getPriceAsFloat() {
        return money.getAmount().floatValue();
    }
    // TODO: 06.06.2020 no need to export BigDecimal if we can export Money ?

    @Override
    @NonNull
    public BigDecimal getPrice() {
        return money.getAmount();
    }

    @NonNull
    @Override
    public BigMoney getMoney() {
        return money.toBigMoney();
    }

    @Override
    @NonNull
    public String getDecimalFormattedPrice() {
        return decimalFormattedPrice;
    }

    @Override
    @NonNull
    public String getCurrencyFormattedPrice() {
        return currencyFormattedPrice;
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        return currencyCodeFormattedPrice;
    }

    @Override
    @NonNull
    public CurrencyUnit getCurrency() {
        return money.getCurrencyUnit();
    }

    @Override
    @NonNull
    public String getCurrencyCode() {
        return money.getCurrencyUnit().getCode();
    }

    @Override
    public boolean isSingleCurrency() {
        return true;
    }

    @NonNull
    @Override
    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    @Override
    public String toString() {
        return getCurrencyFormattedPrice();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(money);
        dest.writeSerializable(exchangeRate);
    }

    public static final Creator<SinglePriceImplNew> CREATOR = new Creator<SinglePriceImplNew>() {
        public SinglePriceImplNew createFromParcel(Parcel source) {
            return new SinglePriceImplNew(source);
        }

        public SinglePriceImplNew[] newArray(int size) {
            return new SinglePriceImplNew[size];
        }
    };

}
