package co.smartreceipts.android.model.impl.columns.receipts;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptExtra2Column extends AbstractColumnImpl<Receipt> {

    public ReceiptExtra2Column(int id, @NonNull SyncState syncState, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.EXTRA_EDITTEXT_2, syncState, customOrderId);
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getExtraEditText2();
    }
}
