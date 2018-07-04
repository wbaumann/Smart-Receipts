package co.smartreceipts.android.model.impl.columns.distance;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceRateColumn extends AbstractColumnImpl<Distance> {

    public DistanceRateColumn(int id, @NonNull SyncState syncState) {
        super(id, DistanceColumnDefinitions.ActualDefinition.RATE, syncState);
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getDecimalFormattedRate();
    }

}
