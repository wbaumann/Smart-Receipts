package co.smartreceipts.android.model.impl.columns.categories;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.comparators.ColumnNameComparator;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.ActualColumnDefinition;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;

public class CategoryColumnDefinitions implements ColumnDefinitions<SumCategoryGroupingResult> {

    /**
     * Note: Column types must be unique
     * Column type must be >= 0
     */
    enum ActualDefinition implements ActualColumnDefinition {
        NAME(1, R.string.category_name_field),
        CODE(2, R.string.category_code_field),
        PRICE(3, R.string.category_price_field),
        TAX(4, R.string.category_tax_field),
        CURRENCY(5, R.string.category_currency_field);

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

    private final ActualDefinition[] actualDefinitions;
    private final ReportResourcesManager reportResourcesManager;

    public CategoryColumnDefinitions(@NonNull ReportResourcesManager reportResourcesManager) {
        this.actualDefinitions = ActualDefinition.values();
        this.reportResourcesManager = reportResourcesManager;
    }

    @NonNull
    @Override
    public Column<SumCategoryGroupingResult> getColumn(int id, int columnType, @NonNull SyncState syncState, long ignoredCustomOrderId) {
        for (final ActualDefinition definition : actualDefinitions) {

            if (columnType == definition.columnType) {
                return getColumnFromClass(id, definition, syncState);
            }
        }
        throw new IllegalArgumentException("Unknown column type: " + columnType);
    }

    @NonNull
    @Override
    public List<Column<SumCategoryGroupingResult>> getAllColumns() {
        final ArrayList<AbstractColumnImpl<SumCategoryGroupingResult>> columns = new ArrayList<>(actualDefinitions.length);
        for (final ActualDefinition definition : actualDefinitions) {
            final AbstractColumnImpl<SumCategoryGroupingResult> column =
                    getColumnFromClass(Column.UNKNOWN_ID, definition, new DefaultSyncState());

            columns.add(column);
        }
        Collections.sort(columns, new ColumnNameComparator<>(reportResourcesManager));
        return new ArrayList<>(columns);
    }

    @NonNull
    @Override
    public Column<SumCategoryGroupingResult> getDefaultInsertColumn() {
        return getColumnFromClass(Column.UNKNOWN_ID, ActualDefinition.NAME, new DefaultSyncState());
    }


    private AbstractColumnImpl<SumCategoryGroupingResult> getColumnFromClass(int id, @NonNull ActualDefinition definition, @NonNull SyncState syncState) {
        switch (definition) {
            case NAME:
                return new CategoryNameColumn(id, syncState);
            case CODE:
                return new CategoryCodeColumn(id, syncState);
            case PRICE:
                return new CategoryPriceColumn(id, syncState);
            case TAX:
                return new CategoryTaxColumn(id, syncState);
            case CURRENCY:
                return new CategoryCurrencyColumn(id, syncState);
            default:
                throw new IllegalArgumentException("Unknown definition type: " + definition);
        }
    }
}
