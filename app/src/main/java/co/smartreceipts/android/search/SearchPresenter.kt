package co.smartreceipts.android.search

import co.smartreceipts.android.widget.viper.BaseViperPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchPresenter @Inject constructor(view: SearchView, interactor: SearchInteractor) :
    BaseViperPresenter<SearchView, SearchInteractor>(view, interactor) {

    override fun subscribe() {

        compositeDisposable.add(
            view.inputChanges
                .skipInitialValue()
                .debounce(250, TimeUnit.MILLISECONDS)
                .map { it.trim() }
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapSingle { input: CharSequence -> interactor.getSearchResults(input.toString()) }
                .subscribe { searchResults -> view.presentSearchResults(searchResults) }
        )
    }
}