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
public final class ReportStartDateColumn extends AbstractColumnImpl<Receipt> {

    private final Context localizedContext;
    private final UserPreferenceManager preferences;

    public ReportStartDateColumn(int id, @NonNull SyncState syncState, @NonNull Context localizedContext,
                                 @NonNull UserPreferenceManager preferences, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.REPORT_START_DATE, syncState, customOrderId);
        this.localizedContext = localizedContext;
        this.preferences = preferences;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getTrip().getFormattedStartDate(localizedContext, preferences.get(UserPreference.General.DateSeparator));
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
