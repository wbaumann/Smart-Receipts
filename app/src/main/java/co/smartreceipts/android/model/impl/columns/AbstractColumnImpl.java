package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ActualColumnDefinition;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides an abstract implementation of the column contract to cover shared code
 */
public abstract class AbstractColumnImpl<T> implements Column<T> {

    private final int id;
    private final ActualColumnDefinition columnDefinition;
    private final SyncState syncState;
    private final long customOrderId;

    @Deprecated
    public AbstractColumnImpl(int id, @NonNull ActualColumnDefinition columnDefinition, @NonNull SyncState syncState) {
        this(id, columnDefinition, syncState, 0);
    }

    public AbstractColumnImpl(int id, @NonNull ActualColumnDefinition columnDefinition,
                              @NonNull SyncState syncState, long customOrderId) {
        this.id = id;
        this.columnDefinition = Preconditions.checkNotNull(columnDefinition);
        this.syncState = syncState;
        this.customOrderId = customOrderId;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public long getCustomOrderId() {
        return customOrderId;
    }

    @Override
    public int getType() {
        return columnDefinition.getColumnType();
    }

    @Override
    public int getHeaderStringResId() {
        return columnDefinition.getColumnHeaderId();
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<T> rows) {
        return "";
    }

    @Override
    public int compareTo(@NonNull Column otherColumn) {
        return customOrderId == otherColumn.getCustomOrderId() ? getId() - otherColumn.getId() :
                Long.compare(customOrderId, otherColumn.getCustomOrderId());
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return syncState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractColumnImpl<?> that = (AbstractColumnImpl<?>) o;
        return id == that.id &&
                customOrderId == that.customOrderId &&
                Objects.equals(columnDefinition, that.columnDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, columnDefinition, customOrderId);
    }
}
