package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.ReceiptRow;

public class ReceiptMaximumPriceFilter implements Filter<ReceiptRow> {

	private final static String MAX_PRICE = "maxprice";
	private final static String CURRENCY_CODE = "currencycode";

	private final float mMaxPrice;
	private final String mCurrencyCode;

	public ReceiptMaximumPriceFilter(float maxPrice, String currencyCode) {
		if (currencyCode == null)
			throw new IllegalArgumentException(
					"ReceiptMinPriceFilter requires non-null currencyCode");

		mMaxPrice = maxPrice;
		mCurrencyCode = currencyCode;
	}

	public ReceiptMaximumPriceFilter(JSONObject json) throws JSONException {
		this.mMaxPrice = (float) json.getDouble(MAX_PRICE);
		this.mCurrencyCode = json.getString(CURRENCY_CODE);
	}

	@Override
	public boolean accept(ReceiptRow t) {
		return t.getPriceAsFloat() <= mMaxPrice
				&& t.getCurrencyCode().equalsIgnoreCase(mCurrencyCode);
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(MAX_PRICE, mMaxPrice);
		json.put(CURRENCY_CODE, mCurrencyCode);
		return json;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// TODO is it the correct way to generate hash?
		result = prime * result
				+ ((mCurrencyCode == null) ? 0 : mCurrencyCode.hashCode())
				+ (int) (mMaxPrice * 100);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		ReceiptMaximumPriceFilter other = (ReceiptMaximumPriceFilter) obj;
		return (mMaxPrice == other.mMaxPrice 
				&& mCurrencyCode.equals(other.mCurrencyCode));
	}
}
