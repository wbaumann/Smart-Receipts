package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the total of the price and tax fields based on user settings
 */
public final class ReceiptNetExchangedPriceMinusTaxColumn extends AbstractExchangedPriceColumn {

    private final UserPreferenceManager userPreferenceManager;

    public ReceiptNetExchangedPriceMinusTaxColumn(int id,@NonNull SyncState syncState,
                                                  @NonNull Context localizedContext,
                                                  @NonNull UserPreferenceManager preferences,
                                                  long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.PRICE_MINUS_TAX_EXCHANGED, syncState, localizedContext, customOrderId);
        userPreferenceManager = preferences;
    }

    @NonNull
    @Override
    protected Price getPrice(@NonNull Receipt receipt) {
        if (userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)) {
            return receipt.getPrice();
        } else {
            final PriceBuilderFactory factory = new PriceBuilderFactory(receipt.getPrice());
            factory.setPrice(receipt.getPrice().getPrice().subtract(receipt.getTax().getPrice()));
            return factory.build();
        }
    }
}
