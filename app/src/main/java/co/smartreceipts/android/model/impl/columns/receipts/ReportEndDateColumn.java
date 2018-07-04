package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReportEndDateColumn extends AbstractColumnImpl<Receipt> {

    private final Context context;
    private final UserPreferenceManager preferences;

    public ReportEndDateColumn(int id, @NonNull SyncState syncState, @NonNull Context context,
                               @NonNull UserPreferenceManager preferences, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.REPORT_END_DATE, syncState, customOrderId);
        this.context = context;
        this.preferences = preferences;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getTrip().getFormattedEndDate(context, preferences.get(UserPreference.General.DateSeparator));
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
