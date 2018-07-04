package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptDateColumn extends AbstractColumnImpl<Receipt> {

    private final Context localizedContext;
    private final UserPreferenceManager preferences;

    public ReceiptDateColumn(int id, @NonNull SyncState syncState, @NonNull Context localizedContext,
                             @NonNull UserPreferenceManager preferences, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.DATE, syncState, customOrderId);
        this.localizedContext = localizedContext;
        this.preferences = preferences;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getFormattedDate(localizedContext, preferences.get(UserPreference.General.DateSeparator));
    }
}
