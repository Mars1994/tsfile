package com.corp.delta.tsfile.filter.definition.operators;

import com.corp.delta.tsfile.filter.definition.CrossSeriesFilterExpression;
import com.corp.delta.tsfile.filter.definition.FilterExpression;
import com.corp.delta.tsfile.filter.definition.filterseries.FilterSeries;
import com.corp.delta.tsfile.filter.visitorImpl.FilterVisitor;

/**
 * Either of the left and right operators of CSOr must satisfy the condition.</br>
 * CSOr represents Cross Series Or operation.
 * 
 * @author CGF
 *
 */
public class CSOr extends CrossSeriesFilterExpression {
	public CSOr(FilterExpression left, FilterExpression right) {
		super(left, right);
	}

	public String toString() {
		return "[" + super.left + "]" + " | [" + super.right + "]";
	}

	/**
	 * Not Used
	 * @param vistor
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> T accept(FilterVisitor<T> vistor) {
		return null;
	}

	/**
	 * Not Used
	 * @return
	 */
	@Override
	public FilterSeries<?> getFilterSeries() {
		return null;
	}
}