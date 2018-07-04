package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.ActualColumnDefinition;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.ColumnFinder;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.comparators.ColumnNameComparator;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.SettingUserIdColumn;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;

/**
 * Provides specific definitions for all {@link co.smartreceipts.android.model.Receipt} {@link co.smartreceipts.android.model.Column}
 * objects
 */
public final class ReceiptColumnDefinitions implements ColumnDefinitions<Receipt>, ColumnFinder {

    /**
     * Note: Column types must be unique, because they are saved to the DB
     * Column type must be >= 0
     */
    public enum ActualDefinition implements ActualColumnDefinition {
        BLANK(0, R.string.column_item_blank, R.string.original_column_item_blank_en_us_name),
        CATEGORY_CODE(1, R.string.column_item_category_code, R.string.original_column_item_category_code_en_us_name),
        CATEGORY_NAME(2, R.string.column_item_category_name, R.string.original_column_item_category_name_en_us_name),
        USER_ID(3, R.string.column_item_user_id, R.string.original_column_item_user_id_en_us_name),
        REPORT_NAME(4, R.string.column_item_report_name, R.string.original_column_item_report_name_en_us_name),
        REPORT_START_DATE(5, R.string.column_item_report_start_date, R.string.original_column_item_report_start_date_en_us_name),
        REPORT_END_DATE(6, R.string.column_item_report_end_date, R.string.original_column_item_report_end_date_en_us_name),
        REPORT_COMMENT(7, R.string.column_item_report_comment, R.string.original_column_item_report_comment_en_us_name),
        REPORT_COST_CENTER(8, R.string.column_item_report_cost_center, R.string.original_column_item_report_cost_center_en_us_name),
        IMAGE_FILE_NAME(9, R.string.column_item_image_file_name, R.string.original_column_item_image_file_name_en_us_name),
        IMAGE_PATH(10, R.string.column_item_image_path, R.string.original_column_item_image_path_en_us_name),
        COMMENT(11, R.string.RECEIPTMENU_FIELD_COMMENT, R.string.original_column_RECEIPTMENU_FIELD_COMMENT_en_us_name),
        CURRENCY(12, R.string.RECEIPTMENU_FIELD_CURRENCY, R.string.original_column_RECEIPTMENU_FIELD_CURRENCY_en_us_name),
        DATE(13, R.string.RECEIPTMENU_FIELD_DATE, R.string.original_column_RECEIPTMENU_FIELD_DATE_en_us_name),
        NAME(14, R.string.RECEIPTMENU_FIELD_NAME, R.string.original_column_RECEIPTMENU_FIELD_NAME_en_us_name),
        PRICE(15, R.string.RECEIPTMENU_FIELD_PRICE, R.string.original_column_RECEIPTMENU_FIELD_PRICE_en_us_name),
        PRICE_MINUS_TAX(16, R.string.column_item_receipt_price_minus_tax),
        PRICE_EXCHANGED(17, R.string.column_item_converted_price_exchange_rate, R.string.original_column_item_converted_price_exchange_rate_en_us_name),
        TAX(18, R.string.RECEIPTMENU_FIELD_TAX, R.string.original_column_RECEIPTMENU_FIELD_TAX_en_us_name),
        TAX_EXCHANGED(19, R.string.column_item_converted_tax_exchange_rate, R.string.original_column_item_converted_tax_exchange_rate_en_us_name),
        PRICE_PLUS_TAX_EXCHANGED(20, R.string.column_item_converted_price_plus_tax_exchange_rate, R.string.original_column_item_converted_price_plus_tax_exchange_rate_en_us_name),
        PRICE_MINUS_TAX_EXCHANGED(21, R.string.column_item_converted_price_minus_tax_exchange_rate),
        EXCHANGE_RATE(22, R.string.column_item_exchange_rate, R.string.original_column_item_exchange_rate_en_us_name),
        PICTURED(23, R.string.column_item_pictured, R.string.original_column_item_pictured_en_us_name),
        REIMBURSABLE(24, R.string.column_item_reimbursable, R.string.original_column_item_reimbursable_en_us_name, R.string.column_item_deprecated_expensable),
        INDEX(25, R.string.column_item_index, R.string.original_column_item_index_en_us_name),
        ID(26, R.string.column_item_id, R.string.original_column_item_id_en_us_name),
        PAYMENT_METHOD(27, R.string.column_item_payment_method),
        EXTRA_EDITTEXT_1(28, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1),
        EXTRA_EDITTEXT_2(29, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2),
        EXTRA_EDITTEXT_3(30, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);

        private final int columnType;
        private final int stringResId;
        private final List<Integer> legacyStringResIds;

        ActualDefinition(int columnType, @StringRes int stringResId) {
            this.columnType = columnType;
            this.stringResId = stringResId;
            this.legacyStringResIds = Collections.emptyList();
        }

        /**
         * Allows us to specify a legacy item that we've updated our name from, since columns are keyed off the name itself (so what happens
         * if we change the column name... probably not the best design here but we'll revisit later)
         *
         * @param columnType        the type number of the column
         * @param stringResId       the current id
         * @param legacyStringResId the legacy id
         */
        ActualDefinition(int columnType, @StringRes int stringResId, @StringRes int legacyStringResId) {
            this.columnType = columnType;
            this.stringResId = stringResId;
            this.legacyStringResIds = Collections.singletonList(legacyStringResId);
        }

        /**
         * Allows us to specify a legacy item that we've updated our name from, since columns are keyed off the name itself (so what happens
         * if we change the column name... probably not the best design here but we'll revisit later)
         *
         * @param columnType         the type number of the column
         * @param stringResId        the current id
         * @param legacyStringResIds the list of legacy id
         */
        ActualDefinition(int columnType, @StringRes int stringResId, @StringRes int... legacyStringResIds) {
            this.columnType = columnType;
            this.stringResId = stringResId;
            this.legacyStringResIds = new ArrayList<>();
            if (legacyStringResIds != null) {
                for (final int legacyStringResId : legacyStringResIds) {
                    this.legacyStringResIds.add(legacyStringResId);
                }
            }

        }

        @Override
        public final int getColumnType() {
            return columnType;
        }

        @Override
        public int getColumnHeaderId() {
            return stringResId;
        }
    }


    private final ReportResourcesManager reportResourcesManager;
    private final UserPreferenceManager preferences;
    private final ActualDefinition[] actualDefinitions = ActualDefinition.values();

    @Inject
    public ReceiptColumnDefinitions(ReportResourcesManager reportResourcesManager, UserPreferenceManager preferences) {
        this.reportResourcesManager = reportResourcesManager;
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public Column<Receipt> getColumn(int id, int columnType, @NonNull SyncState syncState, long customOrderId) {
        for (final ActualDefinition definition : actualDefinitions) {
            if (columnType == definition.columnType) {
                return getColumnFromClass(id, definition, syncState, customOrderId);
            }
        }

        throw new IllegalArgumentException("Unknown column type: " + columnType);
    }

    @NonNull
    @Override
    public List<Column<Receipt>> getAllColumns() {
        final ArrayList<AbstractColumnImpl<Receipt>> columns = new ArrayList<>();
        for (final ActualDefinition definition : actualDefinitions) {

            // don't add column if column name is empty (useful for flex cases)
            if (!reportResourcesManager.getFlexString(definition.getColumnHeaderId()).isEmpty()) {

                final AbstractColumnImpl<Receipt> column = getColumnFromClass(Column.UNKNOWN_ID, definition, new DefaultSyncState());
                columns.add(column);
            }

        }
        Collections.sort(columns, new ColumnNameComparator<>(reportResourcesManager));
        return new ArrayList<>(columns);
    }

    @NonNull
    @Override
    public Column<Receipt> getDefaultInsertColumn() {
        return new BlankColumn<>(Column.UNKNOWN_ID, new DefaultSyncState(), Long.MAX_VALUE);
    }

    public List<Column<Receipt>> getCsvDefaults() {
        // TODO: Re-design how these are added
        final ArrayList<Column<Receipt>> columns = new ArrayList<>();
        columns.add(getColumn(ActualDefinition.DATE));
        columns.add(getColumn(ActualDefinition.NAME));
        columns.add(getColumn(ActualDefinition.PRICE));
        columns.add(getColumn(ActualDefinition.CURRENCY));
        columns.add(getColumn(ActualDefinition.CATEGORY_NAME));
        columns.add(getColumn(ActualDefinition.CATEGORY_CODE));
        columns.add(getColumn(ActualDefinition.COMMENT));
        columns.add(getColumn(ActualDefinition.REIMBURSABLE));
        return columns;

    }

    public List<Column<Receipt>> getPdfDefaults() {
        // TODO: Re-design how these are added
        final ArrayList<Column<Receipt>> columns = new ArrayList<>();
        columns.add(getColumn(ActualDefinition.DATE));
        columns.add(getColumn(ActualDefinition.NAME));
        columns.add(getColumn(ActualDefinition.PRICE));
        columns.add(getColumn(ActualDefinition.CURRENCY));
        columns.add(getColumn(ActualDefinition.CATEGORY_NAME));
        columns.add(getColumn(ActualDefinition.REIMBURSABLE));
        return columns;
    }

    @Override
    public int getColumnTypeByHeaderValue(String header) {

        for (ActualDefinition actualDefinition : actualDefinitions) {
            if (reportResourcesManager.getFlexString(actualDefinition.stringResId).equals(header)) {
                return actualDefinition.getColumnType();
            }
            for (Integer legacyStringResId : actualDefinition.legacyStringResIds) {
                if (legacyStringResId > 0 && reportResourcesManager.getFlexString(legacyStringResId).equals(header)) {
                    return actualDefinition.getColumnType();
                }
            }

        }

        return -1;
    }


    @NonNull
    private Column<Receipt> getColumn(@NonNull ActualDefinition actualDefinition) {
        return getColumnFromClass(Column.UNKNOWN_ID, actualDefinition, new DefaultSyncState());
    }

    @NonNull
    private AbstractColumnImpl<Receipt> getColumnFromClass(int id, @NonNull ActualDefinition definition,
                                                           @NonNull SyncState syncState) {
        return getColumnFromClass(id, definition, syncState, 0);
    }

    @NonNull
    private AbstractColumnImpl<Receipt> getColumnFromClass(int id, @NonNull ActualDefinition definition,
                                                           @NonNull SyncState syncState, long customOrderId) {
        final Context localizedContext = reportResourcesManager.getLocalizedContext();

        // TODO: 17.06.2018 convert column classes to Kotlin
        switch (definition) {
            case BLANK:
                return new BlankColumn<>(id, syncState, customOrderId);
            case CATEGORY_CODE:
                return new ReceiptCategoryCodeColumn(id, syncState, customOrderId);
            case CATEGORY_NAME:
                return new ReceiptCategoryNameColumn(id, syncState, customOrderId);
            case USER_ID:
                return new SettingUserIdColumn<>(id, syncState, preferences, customOrderId);
            case REPORT_NAME:
                return new ReportNameColumn(id, syncState, customOrderId);
            case REPORT_START_DATE:
                return new ReportStartDateColumn(id, syncState, localizedContext, preferences, customOrderId);
            case REPORT_END_DATE:
                return new ReportEndDateColumn(id, syncState, localizedContext, preferences, customOrderId);
            case REPORT_COMMENT:
                return new ReportCommentColumn(id, syncState, customOrderId);
            case REPORT_COST_CENTER:
                return new ReportCostCenterColumn(id, syncState, customOrderId);
            case IMAGE_FILE_NAME:
                return new ReceiptFileNameColumn(id, syncState, customOrderId);
            case IMAGE_PATH:
                return new ReceiptFilePathColumn(id, syncState, customOrderId);
            case COMMENT:
                return new ReceiptCommentColumn(id, syncState, customOrderId);
            case CURRENCY:
                return new ReceiptCurrencyCodeColumn(id, syncState, customOrderId);
            case DATE:
                return new ReceiptDateColumn(id, syncState, localizedContext, preferences, customOrderId);
            case NAME:
                return new ReceiptNameColumn(id, syncState, customOrderId);
            case PRICE:
                return new ReceiptPriceColumn(id, syncState, customOrderId);
            case PRICE_MINUS_TAX:
                return new ReceiptPriceMinusTaxColumn(id, syncState, preferences, customOrderId);
            case PRICE_EXCHANGED:
                return new ReceiptExchangedPriceColumn(id, syncState, localizedContext, customOrderId);
            case TAX:
                return new ReceiptTaxColumn(id, syncState, customOrderId);
            case TAX_EXCHANGED:
                return new ReceiptExchangedTaxColumn(id, syncState, localizedContext, customOrderId);
            case PRICE_PLUS_TAX_EXCHANGED:
                return new ReceiptNetExchangedPricePlusTaxColumn(id, syncState, localizedContext, preferences, customOrderId);
            case PRICE_MINUS_TAX_EXCHANGED:
                return new ReceiptNetExchangedPriceMinusTaxColumn(id, syncState, localizedContext, preferences, customOrderId);
            case EXCHANGE_RATE:
                return new ReceiptExchangeRateColumn(id, syncState, customOrderId);
            case PICTURED:
                return new ReceiptIsPicturedColumn(id, syncState, localizedContext, customOrderId);
            case REIMBURSABLE:
                return new ReceiptIsReimbursableColumn(id, syncState, localizedContext, customOrderId);
            case INDEX:
                return new ReceiptIndexColumn(id, syncState, customOrderId);
            case ID:
                return new ReceiptIdColumn(id, syncState, customOrderId);
            case PAYMENT_METHOD:
                return new ReceiptPaymentMethodColumn(id, syncState, customOrderId);
            case EXTRA_EDITTEXT_1:
                return new ReceiptExtra1Column(id, syncState, customOrderId);
            case EXTRA_EDITTEXT_2:
                return new ReceiptExtra2Column(id, syncState, customOrderId);
            case EXTRA_EDITTEXT_3:
                return new ReceiptExtra3Column(id, syncState, customOrderId);
            default:
                throw new IllegalArgumentException("Unknown definition type: " + definition);
        }
    }

}
