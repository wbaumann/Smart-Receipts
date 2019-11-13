package co.smartreceipts.android.search

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.persistence.database.tables.CategoriesTable
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable
import co.smartreceipts.android.persistence.database.tables.TripsTable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class SearchInteractor(
    private val databaseHelper: DatabaseHelper,
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    data class SearchResults(val trips: List<Trip>, val receipts: List<Receipt>) {
        fun isEmpty() = trips.isEmpty() && receipts.isEmpty()
    }


    @Inject
    constructor(databaseHelper: DatabaseHelper) : this(databaseHelper, Schedulers.io(), AndroidSchedulers.mainThread())

    fun getSearchResults(input: String): Single<SearchResults> {

        // TODO: 10.11.2019 investigate how we can perform search by date

        if (input.isEmpty()) {
            return Single.just(SearchResults(emptyList(), emptyList()))
        }

        // search by category name and code
        val categoriesList = databaseHelper.search(
            input, CategoriesTable.TABLE_NAME, CategoriesTable.COLUMN_ID, null,
            CategoriesTable.COLUMN_NAME, CategoriesTable.COLUMN_CODE
        )

        // search by payment method name
        val paymentMethodsList = databaseHelper.search(
            input, PaymentMethodsTable.TABLE_NAME, PaymentMethodsTable.COLUMN_ID, null,
            PaymentMethodsTable.COLUMN_METHOD
        )

        // search by trip's name, comment
        val tripsIdList = databaseHelper.search(
            input, TripsTable.TABLE_NAME, TripsTable.COLUMN_ID, TripsTable.COLUMN_FROM,
            TripsTable.COLUMN_NAME, TripsTable.COLUMN_COMMENT
        )

        // search by receipt's name, comment, price
        val receiptsIdList = databaseHelper.search(
            input, ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_DATE,
            ReceiptsTable.COLUMN_NAME, ReceiptsTable.COLUMN_COMMENT, ReceiptsTable.COLUMN_PRICE
        )

        // searching receipts by category id
        for (categoryId in categoriesList) {
            receiptsIdList.addAll(
                databaseHelper.search(
                    categoryId, ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID, null,
                    ReceiptsTable.COLUMN_CATEGORY_ID
                )
            )
        }

        // searching receipts by payment method id
        for (paymentMethodId in paymentMethodsList) {
            receiptsIdList.addAll(
                databaseHelper.search(
                    paymentMethodId, ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID, null,
                    ReceiptsTable.COLUMN_PAYMENT_METHOD_ID
                )
            )
        }

        return Single.zip(

            databaseHelper.receiptsTable.get()
                .map { allReceipts ->
                    val results = ArrayList<Receipt>()

                    for (receipt in allReceipts) {
                        if (receiptsIdList.contains(receipt.id.toString())) {
                            results.add(receipt)
                        }
                    }

                    return@map results
                },

            databaseHelper.tripsTable.get()
                .map { allTrips ->
                    val results = ArrayList<Trip>()

                    for (trip in allTrips) {
                        if (tripsIdList.contains(trip.id.toString())) {
                            results.add(trip)
                        }
                    }

                    return@map results
                },
            BiFunction<List<Receipt>, List<Trip>, SearchResults> { receipts: List<Receipt>, trips: List<Trip> ->
                SearchResults(trips.sorted(), receipts.sorted())
            }
        )
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }
}