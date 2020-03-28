package co.smartreceipts.android.distance.editor

import co.smartreceipts.android.widget.viper.BaseViperPresenter
import co.smartreceipts.core.di.scopes.FragmentScope
import javax.inject.Inject

@FragmentScope
class DistanceAutoCompletePresenter @Inject constructor(
    view: DistanceCreateEditView, interactor: DistanceCreateEditInteractor
) :
    BaseViperPresenter<DistanceCreateEditView, DistanceCreateEditInteractor>(view, interactor) {

    override fun subscribe() {

        compositeDisposable.add(view.hideAutoCompleteVisibilityClick
                .flatMap { t ->
                    interactor.updateDistance(t.from, t.to)
                }
                .subscribe {
                    view.hideAutoCompleteValue(it.isPresent)
                }
        )

        compositeDisposable.add(view.unHideAutoCompleteVisibilityClick
                .flatMap { t ->
                    interactor.updateDistance(t.from, t.to)
                }
                .subscribe {
                    view.unHideAutoCompleteValue(it.isPresent)
                }
        )
    }

}