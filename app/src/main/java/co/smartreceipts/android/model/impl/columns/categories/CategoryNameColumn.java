package co.smartreceipts.android.model.impl.columns.categories;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.sync.model.SyncState;

public class CategoryNameColumn extends AbstractColumnImpl<SumCategoryGroupingResult> {

    public CategoryNameColumn(int id, @NonNull SyncState syncState) {
        super(id, CategoryColumnDefinitions.ActualDefinition.NAME, syncState);
    }

    @NonNull
    @Override
    public String getValue(@NonNull SumCategoryGroupingResult sumCategoryGroupingResult) {
        return sumCategoryGroupingResult.getCategory().getName() + " [" + sumCategoryGroupingResult.getReceiptsCount() + "]";
    }

}
