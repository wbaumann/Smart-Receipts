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
public final class ReceiptIsReimbursableColumn extends AbstractColumnImpl<Receipt> {

    private final Context localizedContext;

    public ReceiptIsReimbursableColumn(int id, @NonNull SyncState syncState,
                                       @NonNull Context localizedContext, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.REIMBURSABLE, syncState, customOrderId);
        this.localizedContext = localizedContext;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return (receipt.isReimbursable()) ? localizedContext.getString(R.string.yes) : localizedContext.getString(R.string.no);
    }
}
