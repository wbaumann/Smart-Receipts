package co.smartreceipts.android.model

import co.smartreceipts.android.autocomplete.AutoCompleteField

data class AutoCompleteUpdateEvent<Type>(
    val item: Type,
    val type: AutoCompleteField
)
