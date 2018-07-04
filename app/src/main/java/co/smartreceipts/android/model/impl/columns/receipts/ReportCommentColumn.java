package co.smartreceipts.android.model.impl.columns.receipts;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReportCommentColumn extends AbstractColumnImpl<Receipt> {

    public ReportCommentColumn(int id, @NonNull SyncState syncState, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.REPORT_COMMENT, syncState, customOrderId);
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getTrip().getComment();
    }

    @NonNull
    @Override
    public String getFooter(@NonNull List<Receipt> rows) {
        if (!rows.isEmpty()) {
            return getValue(rows.get(0));
        } else {
            return "";
        }
    }
}
