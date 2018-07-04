package co.smartreceipts.android.model.impl.columns.categories;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.sync.model.SyncState;


public class CategoryCodeColumn extends AbstractColumnImpl<SumCategoryGroupingResult> {

    public CategoryCodeColumn(int id, @NonNull SyncState syncState) {
        super(id, CategoryColumnDefinitions.ActualDefinition.CODE, syncState);
    }

    @NonNull
    @Override
    public String getValue(@NonNull SumCategoryGroupingResult sumCategoryGroupingResult) {
        return sumCategoryGroupingResult.getCategory().getCode();
    }

}
