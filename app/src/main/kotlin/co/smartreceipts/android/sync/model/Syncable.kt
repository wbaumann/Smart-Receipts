package co.smartreceipts.android.sync.model

import java.util.*

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
     * The UUID for this item
     *
     * @return the items's UUID
     */
    val uuid: UUID

    /**
     * The current [SyncState] associated with this item
     */
    val syncState: SyncState

    companion object {
        const val MISSING_ID: Int = -1
        val MISSING_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}

