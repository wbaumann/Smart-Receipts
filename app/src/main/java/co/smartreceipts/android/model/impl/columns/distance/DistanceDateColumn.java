package co.smartreceipts.android.model.impl.columns.distance;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceDateColumn extends AbstractColumnImpl<Distance> {

    private final Context localizedContext;
    private final UserPreferenceManager preferences;

    public DistanceDateColumn(int id, @NonNull SyncState syncState, @NonNull Context localizedContext,
                              @NonNull UserPreferenceManager preferences) {
        super(id, DistanceColumnDefinitions.ActualDefinition.DATE, syncState);
        this.localizedContext = localizedContext;
        this.preferences = preferences;
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getFormattedDate(localizedContext, preferences.get(UserPreference.General.DateSeparator));
    }

}
