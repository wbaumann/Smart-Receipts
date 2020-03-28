package co.smartreceipts.android.model

data class AutoCompleteClickEvent<Type>(
    val from: Type,
    val to: Type
)
