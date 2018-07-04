package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPriceColumn;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class PDFTableTest {

    // Class Under Test
    PDFTable pdfTable;

    @Mock
    ReportResourcesManager reportResourcesManager;

    @Mock
    UserPreferenceManager preferences;

    @Mock
    SQLiteDatabase sqliteDatabase;

    @Mock
    TableDefaultsCustomizer tableDefaultsCustomizer;

    @Mock
    OrderingPreferencesManager orderingPreferencesManager;

    SQLiteOpenHelper mSQLiteOpenHelper;


    SQLiteOpenHelper openHelper;

    @Captor
    ArgumentCaptor<String> sqlCaptor;

    Column<Receipt> receiptNameColumn;
    Column<Receipt> receiptPriceColumn;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        openHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        final ReceiptColumnDefinitions receiptColumnDefinitions = new ReceiptColumnDefinitions(reportResourcesManager, preferences);
        pdfTable = new PDFTable(openHelper, receiptColumnDefinitions, orderingPreferencesManager);

        // Now create the table and insert some defaults
        pdfTable.onCreate(openHelper.getWritableDatabase(), tableDefaultsCustomizer);
        receiptNameColumn = pdfTable.insert(new ReceiptNameColumn(-1, new DefaultSyncState(), 0), new DatabaseOperationMetadata()).blockingGet();
        receiptPriceColumn = pdfTable.insert(new ReceiptPriceColumn(-1, new DefaultSyncState(), 0), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(receiptNameColumn);
        assertNotNull(receiptPriceColumn);
    }

    @After
    public void tearDown() {
        openHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + pdfTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("pdfcolumns", pdfTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        pdfTable.onCreate(sqliteDatabase, customizer);
        verify(sqliteDatabase).execSQL(sqlCaptor.capture());
        verify(customizer).insertPDFDefaults(pdfTable);

        assertTrue(sqlCaptor.getValue().contains("CREATE TABLE pdfcolumns"));
        assertTrue(sqlCaptor.getValue().contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(sqlCaptor.getValue().contains("column_type INTEGER DEFAULT 0"));
        assertTrue(sqlCaptor.getValue().contains("drive_sync_id TEXT"));
        assertTrue(sqlCaptor.getValue().contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(sqlCaptor.getValue().contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(sqlCaptor.getValue().contains("last_local_modification_time DATE"));
        assertTrue(sqlCaptor.getValue().contains("custom_order_id INTEGER DEFAULT 0"));
    }

    @Test
    public void onUpgradeFromV9() {
        final int oldVersion = 9;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        pdfTable.onUpgrade(sqliteDatabase, oldVersion, newVersion, customizer);
        verify(sqliteDatabase, atLeastOnce()).execSQL(sqlCaptor.capture());
        verify(customizer).insertPDFDefaults(pdfTable);

        assertTrue(sqlCaptor.getAllValues().get(0).contains(PDFTable.TABLE_NAME));
        assertTrue(sqlCaptor.getAllValues().get(0).contains(PDFTable.idColumnName));
        assertTrue(sqlCaptor.getAllValues().get(0).contains(PDFTable.typeColumnName));
        assertEquals(sqlCaptor.getAllValues().get(0), "CREATE TABLE pdfcolumns (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT);");
        assertEquals(sqlCaptor.getAllValues().get(1), "ALTER TABLE " + pdfTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(sqlCaptor.getAllValues().get(2), "ALTER TABLE " + pdfTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(sqlCaptor.getAllValues().get(3), "ALTER TABLE " + pdfTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(sqlCaptor.getAllValues().get(4), "ALTER TABLE " + pdfTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        pdfTable.onUpgrade(sqliteDatabase, oldVersion, newVersion, customizer);
        verify(sqliteDatabase, atLeastOnce()).execSQL(sqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(pdfTable);

        assertEquals(sqlCaptor.getAllValues().get(0), "ALTER TABLE " + pdfTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(sqlCaptor.getAllValues().get(1), "ALTER TABLE " + pdfTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(sqlCaptor.getAllValues().get(2), "ALTER TABLE " + pdfTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(sqlCaptor.getAllValues().get(3), "ALTER TABLE " + pdfTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV15() {
        final int oldVersion = 15;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        pdfTable.onUpgrade(sqliteDatabase, oldVersion, newVersion, customizer);
        verify(sqliteDatabase, atLeastOnce()).execSQL(sqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(pdfTable);

        assertEquals(sqlCaptor.getAllValues().get(0), "ALTER TABLE " + pdfTable.getTableName() + " ADD COLUMN custom_order_id INTEGER DEFAULT 0;");
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        pdfTable.onUpgrade(sqliteDatabase, oldVersion, newVersion, customizer);
        verify(sqliteDatabase, never()).execSQL(sqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(pdfTable);
    }

    @Test
    public void get() {
        final List<Column<Receipt>> columns = pdfTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(receiptNameColumn, receiptPriceColumn));
    }

    @Test
    public void findByPrimaryKey() {
        pdfTable.findByPrimaryKey(receiptNameColumn.getId())
                .test()
                .assertNoErrors()
                .assertValue(receiptNameColumn);
    }

    @Test
    public void findByPrimaryMissingKey() {
        pdfTable.findByPrimaryKey(-1)
                .test()
                .assertError(Exception.class);
    }

    @Test
    public void insert() {
        final Column<Receipt> column = pdfTable.insert(new ReceiptCategoryNameColumn(-1,
                new DefaultSyncState()), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(column);
        assertEquals(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME.getColumnType(), column.getType());
        assertEquals(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME.getColumnHeaderId(), column.getHeaderStringResId());

        final List<Column<Receipt>> columns = pdfTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(receiptNameColumn, receiptPriceColumn, column));
    }

    @Test
    public void insertDefaultColumn() throws Exception {
        Column<Receipt> defaultColumn = new BlankColumn<>(-1, new DefaultSyncState());


        final Column<Receipt> column = pdfTable.insertDefaultColumn().blockingGet();

        // Note: We cannot do an 'equals' operation here, since the inserted column will receive a primary key
        assertNotNull(column);
        assertTrue(column instanceof BlankColumn);
        assertEquals(defaultColumn.getType(), column.getType());
        assertEquals(defaultColumn.getHeaderStringResId(), column.getHeaderStringResId());

        final List<Column<Receipt>> columns = pdfTable.get().blockingGet();
        assertEquals(Arrays.asList(receiptNameColumn, receiptPriceColumn, column), columns);
    }

    @Test
    public void update() {
        final Column<Receipt> column = pdfTable.update(receiptNameColumn,
                new ReceiptCategoryNameColumn(-1, new DefaultSyncState())
                , new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(column);
        assertEquals(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME.getColumnType(), column.getType());
        assertEquals(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME.getColumnHeaderId(), column.getHeaderStringResId());

        final List<Column<Receipt>> columns = pdfTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(column, receiptPriceColumn));
    }

    @Test
    public void delete() {
        assertEquals(receiptNameColumn, pdfTable.delete(receiptNameColumn, new DatabaseOperationMetadata()).blockingGet());
        assertEquals(pdfTable.get().blockingGet(), Collections.singletonList(receiptPriceColumn));
    }

    @Test
    public void deleteLast() {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        assertTrue(pdfTable.deleteLast(databaseOperationMetadata).blockingGet());
        assertEquals(pdfTable.get().blockingGet(), Collections.singletonList(receiptNameColumn));
        assertTrue(pdfTable.deleteLast(databaseOperationMetadata).blockingGet());
        assertEquals(pdfTable.get().blockingGet(), Collections.emptyList());
        assertFalse(pdfTable.deleteLast(databaseOperationMetadata).blockingGet());
    }

}
