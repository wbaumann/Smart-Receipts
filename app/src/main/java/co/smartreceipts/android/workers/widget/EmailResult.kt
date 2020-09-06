package co.smartreceipts.android.workers.widget

sealed class EmailResult {


    class Error(val errorType: GenerationErrors) : EmailResult()

    class Success(val s: String): EmailResult()

    object InProgress: EmailResult()
}

enum class GenerationErrors {
    ERROR_UNDETERMINED,
    ERROR_NO_SELECTION,
    ERROR_NO_RECEIPTS,
    ERROR_DISABLED_DISTANCES,
    ERROR_TOO_MANY_COLUMNS,
    ERROR_GENERAL_PDF,
    ERROR_MEMORY
}