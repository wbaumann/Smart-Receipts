package co.smartreceipts.android.workers.widget

import android.content.Context
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.persistence.PersistenceManager
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.workers.EmailAssistantKt
import co.smartreceipts.android.workers.EmailAssistantKt.EmailOptions
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import co.smartreceipts.core.di.scopes.ApplicationScope
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@ApplicationScope
class GenerateReportInteractor constructor(
    private val context: Context,
    private val persistenceManager: PersistenceManager, // TODO: 30.08.2020 refactoring
    private val purchaseWallet: PurchaseWallet,
    private val preferenceManager: UserPreferenceManager,
    private val reportResourcesManager: ReportResourcesManager,
    private val dateFormatter: DateFormatter,
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    @Inject
    constructor(
        context: Context,
        persistenceManager: PersistenceManager,
        purchaseWallet: PurchaseWallet,
        preferenceManager: UserPreferenceManager,
        reportResourcesManager: ReportResourcesManager,
        dateFormatter: DateFormatter
    ) : this(
        context,
        persistenceManager,
        purchaseWallet,
        preferenceManager,
        reportResourcesManager,
        dateFormatter,
        Schedulers.io(),
        AndroidSchedulers.mainThread()
    )

    fun generateReport(trip: Trip, options: EnumSet<EmailOptions>): Single<EmailResult> {

        val assistant =
            EmailAssistantKt(context, reportResourcesManager, persistenceManager, preferenceManager, purchaseWallet, dateFormatter)

        return assistant.emailTrip(trip, options)
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)

    }
}