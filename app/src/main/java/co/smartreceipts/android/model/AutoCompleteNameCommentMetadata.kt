package co.smartreceipts.android.model

interface AutoCompleteNameCommentMetadata: AutoCompleteCommentMetadata {
    /**
     *  Checks if the name of object should be shown in auto complete results
     */
    val isNameHiddenFromAutoComplete: Boolean
}
