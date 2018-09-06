package co.smartreceipts.android.sync.model

/**
 * Marks a particular model object as capable of being synced with a remote server environment
 */
interface Syncable {

    /**
     * The primary key id for this item
     *
     * @return the items's autoincrement id
     */
    val id: Int

    /**
     * The current [SyncState] associated with this item
     */
    val syncState: SyncState
}
