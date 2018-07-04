package co.smartreceipts.android.model.impl.columns.distance;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceCommentColumn extends AbstractColumnImpl<Distance> {

    public DistanceCommentColumn(int id, @NonNull SyncState syncState) {
        super(id, DistanceColumnDefinitions.ActualDefinition.COMMENT, syncState);
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getComment();
    }

}
