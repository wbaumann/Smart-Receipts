package co.smartreceipts.android.receipts.editor.paymentmethods

import co.smartreceipts.android.model.PaymentMethod
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.rx.RxSchedulers
import co.smartreceipts.android.widget.mvp.BasePresenter
import co.smartreceipts.core.di.scopes.FragmentScope
import io.reactivex.Scheduler
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Named

/**
 * A default presenter implementation to manage displaying the current receipt price and tax values
 */
@FragmentScope
class PaymentMethodsPresenter @Inject constructor(view: PaymentMethodsView,
                                                  private val userPreferenceManager: UserPreferenceManager,
                                                  @Named(RxSchedulers.IO) private val ioScheduler: Scheduler,
                                                  @Named(RxSchedulers.MAIN) private val mainScheduler: Scheduler,
                                                  private val controller: PaymentMethodsTableController) : BasePresenter<PaymentMethodsView>(view) {

    private val paymentMethodTableEventsListener: TableEventsListener<PaymentMethod> = object : StubTableEventsListener<PaymentMethod>() {
        override fun onGetSuccess(list: List<PaymentMethod>) {
            val paymentMethods = ArrayList(list)
            paymentMethods.add(PaymentMethod.NONE)
            view.displayPaymentMethods(paymentMethods)
        }
    }

    override fun subscribe() {
        compositeDisposable.add(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(view.togglePaymentMethodFieldVisibility()))

        compositeDisposable.add(userPreferenceManager.userPreferenceChangeStream
                .subscribeOn(ioScheduler)
                .filter { it == UserPreference.Receipts.UsePaymentMethods }
                .flatMapSingle { userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods) }
                .observeOn(mainScheduler)
                .subscribe(view.togglePaymentMethodFieldVisibility()))

        controller.subscribe(paymentMethodTableEventsListener)
        controller.get()
    }

    override fun unsubscribe() {
        controller.unsubscribe(paymentMethodTableEventsListener)
        super.unsubscribe()
    }
}
