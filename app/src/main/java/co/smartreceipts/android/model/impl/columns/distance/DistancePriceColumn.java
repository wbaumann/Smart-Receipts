package co.smartreceipts.android.model.impl.columns.distance;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistancePriceColumn extends AbstractColumnImpl<Distance> {

    private final boolean allowSpecialCharacters;

    public DistancePriceColumn(int id, @NonNull SyncState syncState, boolean allowSpecialCharacters) {
        super(id, DistanceColumnDefinitions.ActualDefinition.PRICE, syncState);
        this.allowSpecialCharacters = allowSpecialCharacters;
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        if (allowSpecialCharacters) {
            return distance.getPrice().getCurrencyFormattedPrice();
        } else {
            return distance.getPrice().getCurrencyCodeFormattedPrice();
        }
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<Distance> distances) {
        final PriceCurrency tripCurrency = !distances.isEmpty() ? distances.get(0).getTrip().getTripCurrency() : null;
        if (allowSpecialCharacters) {
            return new PriceBuilderFactory().setPriceables(distances, tripCurrency).build().getCurrencyFormattedPrice();
        } else {
            return new PriceBuilderFactory().setPriceables(distances, tripCurrency).build().getCurrencyCodeFormattedPrice();
        }
    }
}
