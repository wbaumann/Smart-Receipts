package co.smartreceipts.android.filters;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.ReceiptRow;

/**
 * A filter implementation of {@link NotFilter} for {@link ReceiptRow}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class ReceiptNotFilter extends NotFilter<ReceiptRow>{

	public ReceiptNotFilter() {
		super();
	}
	
	public ReceiptNotFilter(List<Filter<ReceiptRow>> filters) {
		super(filters);
	}
	
	protected ReceiptNotFilter(JSONObject json) throws JSONException {
		super(json);
	}
	
	
	@Override
	Filter<ReceiptRow> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getReceiptFilter(json);
	}

}
