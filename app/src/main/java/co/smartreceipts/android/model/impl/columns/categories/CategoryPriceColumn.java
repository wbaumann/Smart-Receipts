package co.smartreceipts.android.model.impl.columns.categories;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.sync.model.SyncState;


public class CategoryPriceColumn extends AbstractColumnImpl<SumCategoryGroupingResult> {

    public CategoryPriceColumn(int id, @NonNull SyncState syncState) {
        super(id, CategoryColumnDefinitions.ActualDefinition.PRICE, syncState);
    }

    @NonNull
    @Override
    public String getValue(@NonNull SumCategoryGroupingResult sumCategoryGroupingResult) {
        return sumCategoryGroupingResult.getPrice().getDecimalFormattedPrice();
    }

    @NonNull
    @Override
    public String getFooter(@NonNull List<SumCategoryGroupingResult> rows) {
        if (!rows.isEmpty()) {
            final PriceCurrency tripCurrency = rows.get(0).getCurrency();
            final List<Price> prices = new ArrayList<>();
            for (final SumCategoryGroupingResult row : rows) {
                prices.add(row.getPrice());
            }
            
            final Price total =  new PriceBuilderFactory().setPrices(prices, tripCurrency).build();
            if (total.getCurrencyCodeCount() == 1) {
                return total.getDecimalFormattedPrice();
            } else {
                return total.getCurrencyCodeFormattedPrice();
            }
        } else {
            return "";
        }
    }
}
