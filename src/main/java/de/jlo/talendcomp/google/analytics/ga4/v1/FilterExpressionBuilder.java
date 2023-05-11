package de.jlo.talendcomp.google.analytics.ga4.v1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.analytics.data.v1beta.Filter;
import com.google.analytics.data.v1beta.FilterExpression;
import com.google.analytics.data.v1beta.FilterExpressionList;
import com.google.analytics.data.v1beta.NumericValue;
import com.google.analytics.data.v1beta.Filter.NumericFilter;
import com.google.analytics.data.v1beta.Filter.StringFilter;
import com.google.analytics.data.v1beta.Filter.NumericFilter.Operation;
import com.google.analytics.data.v1beta.Filter.StringFilter.MatchType;

public class FilterExpressionBuilder {

	private static Logger log = LogManager.getLogger(FilterExpressionBuilder.class);
	/* Filter operators for String and Numeric filters */
	
	private static final String FILTER_OP_EXACT        = "==";
	private static final String FILTER_OP_EXACT_NOT    = "!=";
	
	private static final String STRING_FILTER_OP_BEGINS_WITH  = "=^";
	private static final String STRING_FILTER_OP_ENDS_WITH    = "=$";
	private static final String STRING_FILTER_OP_REGEX_INCL   = "=~";
	private static final String STRING_FILTER_OP_REGEX_EXCL   = "!~";
	private static final String STRING_FILTER_OP_CONTAINS     = "=@";
	private static final String STRING_FILTER_OP_CONTAINS_NOT = "!@";
		
	/**
	 * Setup filter expressions in th UA style
	 * Examples:
	 * dim1==value1,dim2==value2,dim3!=value3
	 * dim1==value1;dim2==value2;dim3!=value3
	 * 
	 * comma means OR
	 * semicolon means AND
	 * 
	 * @param completeFilter
	 * @param isNumericFilter if true, otherwise it is a String filter
	 * @return FilterExpression object
	 * @throws Exception
	 */
	public static FilterExpression buildCombinedFilterExpression(String completeFilter, boolean isNumericFilter) throws Exception {
		FilterExpression.Builder fb = FilterExpression.newBuilder();
		FilterExpressionList.Builder list = FilterExpressionList.newBuilder();
		boolean or = false;
		boolean and = false;
		StringBuilder oneFilterTerm = new StringBuilder();
		for (int i = 0, n = completeFilter.length(); i < n; i++) {
			char c = completeFilter.charAt(i);
			if (c == '(') {
				// start of group
				throw new Exception("Invalid filter: " + completeFilter + ". Groups are not allowed yet! Position: " + i);
			} else if (c == ')') {
				throw new Exception("Invalid filter: " + completeFilter + ". Groups are not allowed yet! Position: " + i);
			} else if (c == ',') {
				// OR found
				if (i == 0) {
					throw new Exception("Invalid filter: " + completeFilter + ". <,> cannot be the first char! Position: " + i);
				}
				if (and) {
					throw new Exception("Invalid filter: " + completeFilter + ". OR(,) found with previous AND. Within one filter you can only combine all with OR (,) or all with AND(;).  Position: " + i);
				}
				if (oneFilterTerm.length() == 0) {
					throw new Exception("Invalid filter: " + completeFilter + ". OR(,) found but no filter term as operand before. Position: " + i);
				}
				or = true;
				String term = oneFilterTerm.toString();
				oneFilterTerm.setLength(0);
				if (isNumericFilter) {
					if (log.isDebugEnabled()) {
						log.debug("add numeric filter term: " + term);
					}
					list.addExpressions(buildNumericFilterExpression(term));
				} else {
					if (log.isDebugEnabled()) {
						log.debug("add string filter term: " + term);
					}
					list.addExpressions(buildStringFilterExpression(term));
				}
			} else if (c == ';') {
				// AND found
				if (i == 0) {
					throw new Exception("Invalid filter: " + completeFilter + ". <;> cannot be the first char! Position: " + i);
				}
				if (or) {
					throw new Exception("Invalid filter: " + completeFilter + ". AND(;) found with previous OR. Within one filter you can only combine all with OR (,) or all with AND(;). Position: " + i);
				}
				if (oneFilterTerm.length() == 0) {
					throw new Exception("Invalid filter: " + completeFilter + ". AND(;) found but no filter term as operand before. Position: " + i);
				}
				and = true;
				String term = oneFilterTerm.toString();
				oneFilterTerm.setLength(0);
				if (isNumericFilter) {
					if (log.isDebugEnabled()) {
						log.debug("add numeric filter term: " + term);
					}
					list.addExpressions(buildNumericFilterExpression(term));
				} else {
					if (log.isDebugEnabled()) {
						log.debug("add string filter term: " + term);
					}
					list.addExpressions(buildStringFilterExpression(term));
				}
			} else if (c == ' ') {
				// skip this
				continue;
			} else if (i == n-1) {
				// we are at the very last char
				if (oneFilterTerm.length() == 0) {
					throw new Exception("Invalid filter: " + completeFilter + ". Found end of filter but no filter term as operand before. Position: " + i);
				}
				if (and == false && or == false) {
					String term = oneFilterTerm.toString();
					oneFilterTerm.setLength(0);
					if (isNumericFilter) {
						if (log.isDebugEnabled()) {
							log.debug("add numeric filter term: " + term);
						}
						list.addExpressions(buildNumericFilterExpression(term));
					} else {
						if (log.isDebugEnabled()) {
							log.debug("add string filter term: " + term);
						}
						list.addExpressions(buildStringFilterExpression(term));
					}
				} else {
					String term = oneFilterTerm.toString();
					oneFilterTerm.setLength(0);
					if (isNumericFilter) {
						if (log.isDebugEnabled()) {
							log.debug("add numeric filter term: " + term);
						}
						list.addExpressions(buildNumericFilterExpression(term));
					} else {
						if (log.isDebugEnabled()) {
							log.debug("add string filter term: " + term);
						}
						list.addExpressions(buildStringFilterExpression(term));
					}
					if (and) {
						if (log.isDebugEnabled()) {
							log.debug("set AND filter list with count expressions: " + list.getExpressionsCount());
						}
						fb.setAndGroup(list);
					} else if (or) {
						if (log.isDebugEnabled()) {
							log.debug("set OR filter list with count expressions: " + list.getExpressionsCount());
						}
						fb.setOrGroup(list);
					}
				}
			} else {
				// we must be in a filter term
				oneFilterTerm.append(c);
			}
		}
		return fb.build();
	}
	
	public static FilterExpression buildStringFilterExpression(String oneFilterTerm) throws Exception {
		if (oneFilterTerm != null && oneFilterTerm.trim().isEmpty() == false) {
			if (log.isDebugEnabled()) {
				log.debug("Build string filter for term: " + oneFilterTerm);
			}
			String operand1 = null;
			String operand2 = null;
			FilterExpression fe = null;
			int posOp = -1;
			posOp = oneFilterTerm.indexOf(FILTER_OP_EXACT);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setStringFilter(StringFilter.newBuilder()
								.setMatchType(MatchType.EXACT)
								.setCaseSensitive(false)
								.setValue(operand2)))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(FILTER_OP_EXACT_NOT);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setNotExpression(FilterExpression.newBuilder()
							.setFilter(Filter.newBuilder()
								.setFieldName(operand1)
								.setStringFilter(StringFilter.newBuilder()
									.setMatchType(MatchType.EXACT)
									.setCaseSensitive(false)
									.setValue(operand2))))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(STRING_FILTER_OP_BEGINS_WITH);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setStringFilter(StringFilter.newBuilder()
								.setMatchType(MatchType.BEGINS_WITH)
								.setCaseSensitive(false)
								.setValue(operand2)))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(STRING_FILTER_OP_ENDS_WITH);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setStringFilter(StringFilter.newBuilder()
								.setMatchType(MatchType.ENDS_WITH)
								.setCaseSensitive(false)
								.setValue(operand2)))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(STRING_FILTER_OP_REGEX_INCL);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setStringFilter(StringFilter.newBuilder()
								.setMatchType(MatchType.FULL_REGEXP)
								.setCaseSensitive(false)
								.setValue(operand2)))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(STRING_FILTER_OP_REGEX_EXCL);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setNotExpression(FilterExpression.newBuilder()
							.setFilter(Filter.newBuilder()
								.setFieldName(operand1)
								.setStringFilter(StringFilter.newBuilder()
									.setMatchType(MatchType.FULL_REGEXP)
									.setCaseSensitive(false)
									.setValue(operand2))))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(STRING_FILTER_OP_CONTAINS);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setStringFilter(StringFilter.newBuilder()
								.setMatchType(MatchType.CONTAINS)
								.setCaseSensitive(false)
								.setValue(operand2)))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(STRING_FILTER_OP_CONTAINS_NOT);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setNotExpression(FilterExpression.newBuilder()
							.setFilter(Filter.newBuilder()
								.setFieldName(operand1)
								.setStringFilter(StringFilter.newBuilder()
									.setMatchType(MatchType.CONTAINS)
									.setCaseSensitive(false)
							.setValue(operand2))))
						.build();
				return fe;
			}
			if (posOp == -1) {
				throw new Exception("Unknown or missing operator in dimension filter: " + oneFilterTerm);
			}
		}
		return FilterExpression.newBuilder().build(); // dummy
	}

	/* Filter operators for Numeric filters */

	private static final String NUMERIC_FILTER_OP_NOT_EQUALS = "<>";
	private static final String NUMERIC_FILTER_OP_LESS = "<";
	private static final String NUMERIC_FILTER_OP_LESS_OR_EQUALS = "<=";
	private static final String NUMERIC_FILTER_OP_GREATER = ">";
	private static final String NUMERIC_FILTER_OP_GREATER_OR_EQUALS = ">=";

	public static FilterExpression buildNumericFilterExpression(String oneFilterTerm) throws Exception {
		if (oneFilterTerm != null && oneFilterTerm.trim().isEmpty() == false) {
			if (log.isDebugEnabled()) {
				log.debug("Build numeric filter for term: " + oneFilterTerm);
			}
			String operand1 = null;
			String operand2 = null;
			FilterExpression fe = null;
			int posOp = -1;
			posOp = oneFilterTerm.indexOf(FILTER_OP_EXACT);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setNumericFilter(
								NumericFilter.newBuilder()
									.setOperation(Operation.EQUAL)
									.setValue(
										NumericValue.newBuilder()
											.setDoubleValue(Double.parseDouble(operand2)))))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(FILTER_OP_EXACT_NOT);
			if (posOp == -1) {
				posOp = oneFilterTerm.indexOf(NUMERIC_FILTER_OP_NOT_EQUALS);
			}
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setNotExpression(
							FilterExpression.newBuilder()
								.setFilter(Filter.newBuilder()
									.setFieldName(operand1)
									.setNumericFilter(
										NumericFilter.newBuilder()
											.setOperation(Operation.EQUAL)
											.setValue(
													NumericValue.newBuilder()
														.setDoubleValue(Double.parseDouble(operand2))))))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(NUMERIC_FILTER_OP_LESS_OR_EQUALS);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setNumericFilter(NumericFilter.newBuilder()
								.setOperation(Operation.LESS_THAN_OR_EQUAL)
								.setValue(NumericValue.newBuilder()
										.setDoubleValue(Double.parseDouble(operand2)))))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(NUMERIC_FILTER_OP_LESS);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 1).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setNumericFilter(NumericFilter.newBuilder()
								.setOperation(Operation.LESS_THAN)
								.setValue(NumericValue.newBuilder()
										.setDoubleValue(Double.parseDouble(operand2)))))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(NUMERIC_FILTER_OP_GREATER_OR_EQUALS);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 2).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setNumericFilter(NumericFilter.newBuilder()
								.setOperation(Operation.GREATER_THAN_OR_EQUAL)
								.setValue(NumericValue.newBuilder()
										.setDoubleValue(Double.parseDouble(operand2)))))
						.build();
				return fe;
			}
			posOp = oneFilterTerm.indexOf(NUMERIC_FILTER_OP_GREATER);
			if (posOp > 0) {
				operand1 = oneFilterTerm.substring(0, posOp).trim();
				operand2 = oneFilterTerm.substring(posOp + 1).trim();
				fe = FilterExpression.newBuilder()
						.setFilter(Filter.newBuilder()
							.setFieldName(operand1)
							.setNumericFilter(NumericFilter.newBuilder()
								.setOperation(Operation.GREATER_THAN)
								.setValue(NumericValue.newBuilder()
										.setDoubleValue(Double.parseDouble(operand2)))))
						.build();
				return fe;
			}
			if (posOp == -1) {
				throw new Exception("Unknown or missing operator in metric filter: " + oneFilterTerm);
			}
		}
		return FilterExpression.newBuilder().build(); // dummy
	}

}
