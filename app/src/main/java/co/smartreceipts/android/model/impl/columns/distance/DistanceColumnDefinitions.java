package co.smartreceipts.android.model.impl.columns.distance;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.ActualColumnDefinition;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;

/**
 * Provides specific definitions for all {@link co.smartreceipts.android.model.Receipt} {@link co.smartreceipts.android.model.Column}
 * objects
 */
public final class DistanceColumnDefinitions implements ColumnDefinitions<Distance> {

    /**
     * Note: Column types must be unique
     * Column type must be >= 0
     */
    enum ActualDefinition implements ActualColumnDefinition {
        LOCATION(1, R.string.distance_location_field),
        PRICE(2, R.string.distance_price_field),
        DISTANCE(3, R.string.distance_distance_field),
        CURRENCY(4, R.string.dialog_currency_field),
        RATE(5, R.string.distance_rate_field),
        DATE(6, R.string.distance_date_field),
        COMMENT(7, R.string.distance_comment_field);

        private final int columnType;
        private final int stringResId;

        ActualDefinition(int columnType, int stringResId) {
            this.columnType = columnType;
            this.stringResId = stringResId;
        }

        @Override
        public int getColumnType() {
            return columnType;
        }

        @Override
        @StringRes
        public int getColumnHeaderId() {
            return stringResId;
        }
    }


    private final ReportResourcesManager reportResourcesManager;
    private final UserPreferenceManager preferences;
    private final ActualDefinition[] actualDefinitions;
    private final boolean allowSpecialCharacters;

    public DistanceColumnDefinitions(@NonNull ReportResourcesManager reportResourcesManager,
                                     @NonNull UserPreferenceManager preferences,
                                     boolean allowSpecialCharacters) {
        this.reportResourcesManager = reportResourcesManager;
        this.preferences = preferences;
        this.allowSpecialCharacters = allowSpecialCharacters;
        actualDefinitions = ActualDefinition.values();
    }


    @NonNull
    @Override
    public Column<Distance> getColumn(int id, int columnType, @NonNull SyncState syncState, long ignoredCustomOrderId) {
        for (final ActualDefinition definition : actualDefinitions) {
            if (columnType == definition.columnType) {
                return getColumnFromClass(id, definition, syncState);
            }
        }
        throw new IllegalArgumentException("Unknown column type: " + columnType);
    }

    @NonNull
    @Override
    public List<Column<Distance>> getAllColumns() {
        final ArrayList<AbstractColumnImpl<Distance>> columns = new ArrayList<>(actualDefinitions.length);
        for (final ActualDefinition definition : actualDefinitions) {
            final AbstractColumnImpl<Distance> column = getColumnFromClass(Column.UNKNOWN_ID,
                    definition, new DefaultSyncState());
            columns.add(column);

        }
        return new ArrayList<>(columns);
    }

    @NonNull
    @Override
    public Column<Distance> getDefaultInsertColumn() {
        // Hack for the distance default until we let users dynamically set columns. Actually, this will never be called
        return getColumnFromClass(Column.UNKNOWN_ID, ActualDefinition.DISTANCE, new DefaultSyncState());
    }


    @NonNull
    private AbstractColumnImpl<Distance> getColumnFromClass(int id, @NonNull ActualDefinition definition, @NonNull SyncState syncState) {
        final Context localizedContext = reportResourcesManager.getLocalizedContext();

        switch (definition) {
            case LOCATION:
                return new DistanceLocationColumn(id, syncState, localizedContext);
            case PRICE:
                return new DistancePriceColumn(id, syncState, allowSpecialCharacters);
            case DISTANCE:
                return new DistanceDistanceColumn(id, syncState);
            case CURRENCY:
                return new DistanceCurrencyColumn(id, syncState);
            case RATE:
                return new DistanceRateColumn(id, syncState);
            case DATE:
                return new DistanceDateColumn(id, syncState, localizedContext, preferences);
            case COMMENT:
                return new DistanceCommentColumn(id, syncState);
            default:
                throw new IllegalArgumentException("Unknown definition type: " + definition);
        }
    }
}
