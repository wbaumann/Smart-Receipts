package co.smartreceipts.android.model

interface AutoCompleteCommentMetadata {
    /**
     *  Checks if the comment of object should be shown in auto complete results
     */
    val isCommentHiddenFromAutoComplete: Boolean
}
