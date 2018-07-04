package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptIsPicturedColumn extends AbstractColumnImpl<Receipt> {

    private final Context localizedContext;

    public ReceiptIsPicturedColumn(int id, @NonNull SyncState syncState,
                                   @NonNull Context localizedContext, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.PICTURED, syncState, customOrderId);
        this.localizedContext = localizedContext;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        if (receipt.hasImage()) {
            return localizedContext.getString(R.string.yes);
        } else if (receipt.hasPDF()) {
            return localizedContext.getString(R.string.yes_as_pdf);
        } else {
            return localizedContext.getString(R.string.no);
        }
    }
}
