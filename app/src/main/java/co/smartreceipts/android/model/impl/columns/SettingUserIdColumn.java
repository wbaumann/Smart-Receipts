package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns blank values for everything but the header
 */
public final class SettingUserIdColumn<T> extends AbstractColumnImpl<T> {

    private final UserPreferenceManager preferences;

    public SettingUserIdColumn(int id, @NonNull SyncState syncState,
                               @NonNull UserPreferenceManager preferences, long customOrderId) {
        super(id, ReceiptColumnDefinitions.ActualDefinition.USER_ID, syncState, customOrderId);
        this.preferences = preferences;
    }

    @Override
    public String getValue(@NonNull T rowItem) {
        return preferences.get(UserPreference.ReportOutput.UserId);
    }

}
