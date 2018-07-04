package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Converts the {@link co.smartreceipts.android.model.Receipt#getPrice()} based on the current exchange rate
 */
public final class ReceiptExchangedPriceColumn extends AbstractExchangedPriceColumn {

    public ReceiptExchangedPriceColumn(int id, @NonNull SyncState syncState,
                                       @NonNull Context localizedContext, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.PRICE_EXCHANGED, syncState, localizedContext, customOrderId);
    }

    @NonNull
    protected Price getPrice(@NonNull Receipt receipt) {
        return receipt.getPrice();
    }
}
