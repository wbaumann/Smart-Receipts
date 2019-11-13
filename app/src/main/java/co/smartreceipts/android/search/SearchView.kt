package co.smartreceipts.android.search

import com.jakewharton.rxbinding3.InitialValueObservable

interface SearchView {

    val inputChanges: InitialValueObservable<CharSequence>

    fun presentSearchResults(searchResults: SearchInteractor.SearchResults)
}