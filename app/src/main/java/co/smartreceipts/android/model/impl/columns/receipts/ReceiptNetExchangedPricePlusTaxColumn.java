package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the total of the price and tax fields based on user settings
 */
public final class ReceiptNetExchangedPricePlusTaxColumn extends AbstractExchangedPriceColumn {

    private final UserPreferenceManager preferences;

    public ReceiptNetExchangedPricePlusTaxColumn(int id, @NonNull SyncState syncState,
                                                 @NonNull Context localizedContext,
                                                 @NonNull UserPreferenceManager preferences,
                                                 long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.PRICE_PLUS_TAX_EXCHANGED, syncState, localizedContext, customOrderId);
        this.preferences = preferences;
    }

    @NonNull
    @Override
    protected Price getPrice(@NonNull Receipt receipt) {
        if (preferences.get(UserPreference.Receipts.UsePreTaxPrice)) {
            final PriceBuilderFactory factory = new PriceBuilderFactory();
            factory.setPrices(Arrays.asList(receipt.getPrice(), receipt.getTax()), receipt.getTrip().getTripCurrency());
            return factory.build();
        } else {
            return receipt.getPrice();
        }
    }
}
