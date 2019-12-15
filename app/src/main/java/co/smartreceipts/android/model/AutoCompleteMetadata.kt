package co.smartreceipts.android.model

interface AutoCompleteMetadata {
    /**
     *  Checks if the name of object should be shown in auto complete results
     */
    val isNameHiddenFromAutoComplete: Boolean
    /**
     *  Checks if the comment of object should be shown in auto complete results
     */
    val isCommentHiddenFromAutoComplete: Boolean
    /**
     *  Checks if the location of distance should be shown in auto complete results
     */
    val isLocationHiddenFromAutoComplete: Boolean
    /**
     *  Checks if the cost center of trip should be shown in auto complete results
     */
    val isCostCenterHiddenFromAutoComplete: Boolean
}
