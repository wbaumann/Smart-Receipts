package co.smartreceipts.android.model.impl.columns.receipts;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptCategoryCodeColumn extends AbstractColumnImpl<Receipt> {

    public ReceiptCategoryCodeColumn(int id, @NonNull SyncState syncState, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE, syncState, customOrderId);
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getCategory().getCode();
    }

}
