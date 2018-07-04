package co.smartreceipts.android.model.impl.columns.distance;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceLocationColumn extends AbstractColumnImpl<Distance> {

    private final Context localizedContext;

    public DistanceLocationColumn(int id, @NonNull SyncState syncState, @NonNull Context context) {
        super(id, DistanceColumnDefinitions.ActualDefinition.LOCATION, syncState);
        localizedContext = context;
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getLocation();
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<Distance> distances) {
        return localizedContext.getString(R.string.total);
    }
}
