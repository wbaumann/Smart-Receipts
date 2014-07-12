package co.smartreceipts.android.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A filter implementation that combines multiple other {@link Filter}
 * implementations in the manner of a logical NOT operation.
 * 
 * @author Will Baumann
 * @since July 12, 2014
 * 
 */
public abstract class NotFilter<T> implements Filter<T> {

	private static final String NOT_FILTERS = "not_filters";

	private final CopyOnWriteArrayList<Filter<T>> mFilters;

	/**
	 * Additional logical AND calls may be added to this composite
	 * {@link Filter} via the {@link #and(Filter)} method.
	 */
	public NotFilter() {
		mFilters = new CopyOnWriteArrayList<Filter<T>>();
	}

	/**
	 * A preset list of logical NOT filters may be added to this constructor so
	 * that chaining via the {@link #and(Filter)} method is not required
	 * 
	 * @param filters - the {@link List} of {@link Filter} to add
	 */
	public NotFilter(List<Filter<T>> filters) {
		mFilters = new CopyOnWriteArrayList<Filter<T>>(filters);
	}

	/**
	 * A package-private constructor that enables us to recreate this filter via
	 * a {@link JSONObject} representation
	 * 
	 * @param json - the {@link JSONObject} representation of this filter
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	protected NotFilter(JSONObject json) throws JSONException {
		final List<Filter<T>> filters = new ArrayList<Filter<T>>();
		final JSONArray filtersArray = json.getJSONArray(NOT_FILTERS);
		for (int i = 0; i < filtersArray.length(); i++) {
			filters.add(getFilter(filtersArray.getJSONObject(i)));
		}
		mFilters = new CopyOnWriteArrayList<Filter<T>>(filters);
	}

	/**
	 * Retrieves a {@link Filter} implementation from a given JSON object. This
	 * is required in order to properly reconstruct our filters from JSON
	 * 
	 * @param json - the {@link JSONObject} representing a particular filter
	 * @return a {@link Filter} implementation
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	abstract Filter<T> getFilter(JSONObject json) throws JSONException;

	/**
	 * Adds another filter for the logical NOT comparison that will be performed
	 * via the {@link #accept(Object)} method is called.
	 * 
	 * @param filter - the {@link Filter} to add
	 * @return this instance of {@link NotFilter} for method chaining
	 */
	public NotFilter<T> and(final Filter<T> filter) {
		mFilters.add(filter);
		return this;
	}

	@Override
	public boolean accept(T t) {
		// iterating through all filters
		// and return false if one of the filter is accepted
		for (final Filter<T> filter : mFilters) {
			if (filter.accept(t)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONArray filtersArray = new JSONArray();
		for (final Filter<T> filter : mFilters) {
			filtersArray.put(filter.getJsonRepresentation());
		}
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(NOT_FILTERS, filtersArray);
		return json;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mFilters == null) ? 0 : mFilters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		NotFilter<?> other = (NotFilter<?>) obj;
		
		if (mFilters == null) {
			if (other.mFilters != null)
				return false;
		} else if (!mFilters.equals(other.mFilters))
			return false;
		
		return true;
	}

}
