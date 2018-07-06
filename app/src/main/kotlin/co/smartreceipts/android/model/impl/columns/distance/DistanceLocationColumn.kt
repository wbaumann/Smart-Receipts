package co.smartreceipts.android.model.impl.columns.distance

import android.content.Context

import co.smartreceipts.android.R
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.sync.model.SyncState

class DistanceLocationColumn(id: Int, syncState: SyncState, private val localizedContext: Context) :
    AbstractColumnImpl<Distance>(
        id,
        DistanceColumnDefinitions.ActualDefinition.LOCATION,
        syncState
    ) {

    override fun getValue(distance: Distance): String? {
        return distance.location
    }

    override fun getFooter(distances: List<Distance>): String {
        return localizedContext.getString(R.string.total)
    }
}
