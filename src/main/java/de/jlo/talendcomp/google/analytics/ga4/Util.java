/**
 * Copyright 2024 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jlo.talendcomp.google.analytics.ga4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;


public class Util {
	
	private final static Map<String, NumberFormat> numberformatMap = new HashMap<String, NumberFormat>();
	
	public static NumberFormat getNumberFormat(String localeStr) {
		String key = localeStr + "-" + Thread.currentThread().getName();
		NumberFormat nf = (NumberFormat) numberformatMap.get(key);
		if (nf == null) {
			Locale locale = new Locale(localeStr);
			nf = NumberFormat.getInstance(locale);
			numberformatMap.put(key, nf);
		}
		return nf;
	}
	
	public static boolean assertNotEmpty(String s, String message) throws Exception {
		if (s == null || s.trim().isEmpty()) {
			if (message != null) {
				throw new Exception(message);
			} else {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * converts a list of values into a collection
	 * @param valuesSeparated
	 * @param dataType String|date|Double|
	 * @param delimiter
	 * @return
	 */
	public static List<Object> convertToList(String valuesSeparated, String dataType, String delimiter, String pattern) throws Exception {
		List<Object> resultList = new ArrayList<Object>();
		StringTokenizer st = new StringTokenizer(valuesSeparated, delimiter);
		while (st.hasMoreElements()) {
			String value = st.nextToken();
			resultList.add(convertToDatatype(value, dataType, pattern));
		}
		return resultList;
	}
	
	public static Object convertToDatatype(String value, String dataType, String options) throws Exception {
		if (value != null && value.trim().isEmpty() == false) {
			if ("String".equalsIgnoreCase(dataType)) {
				return value;
			} else if ("BigDecimal".equalsIgnoreCase(dataType)) {
				return convertToBigDecimal(value);
			} else if ("Boolean".equalsIgnoreCase(dataType)) {
				return convertToBoolean(value);
			} else if ("Date".equalsIgnoreCase(dataType)) {
				return convertToDate(value, options);
			} else if ("Double".equalsIgnoreCase(dataType)) {
				return convertToDouble(value, options);
			} else if ("Float".equalsIgnoreCase(dataType)) {
				return convertToFloat(value, options);
			} else if ("Int".equalsIgnoreCase(dataType) || "Integer".equalsIgnoreCase(dataType)) {
				return convertToInteger(value, options);
			} else if ("Long".equalsIgnoreCase(dataType)) {
				return convertToLong(value, options);
			} else if ("Short".equalsIgnoreCase(dataType)) {
				return convertToLong(value, options);
			} else if ("Timestamp".equalsIgnoreCase(dataType)) {
				return convertToTimestamp(value, options);
			} else {
				throw new Exception("Unsupported dataType:" + dataType);
			}
		} else {
			return null;
		}
	}
	
	/**
	 * concerts the string format into a Date
	 * @param dateString
	 * @param pattern
	 * @return the resulting Date
	 */
	public static Date convertToDate(String dateString, String pattern) throws Exception {
		if (dateString == null || dateString.trim().isEmpty() || dateString.equals("total")) {
			return null;
		}
		if (pattern == null || pattern.isEmpty()) {
			throw new Exception("convertToDate failed: pattern cannot be null or empty");
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			return sdf.parse(dateString.trim());
		} catch (Throwable t) {
			throw new Exception("Failed to convert string to date:" + t.getMessage(), t);
		}
	}
	
	public static Timestamp convertToTimestamp(String dateString, String pattern) throws Exception {
		Date date = convertToDate(dateString, pattern);
		if (date != null) {
			return new Timestamp(date.getTime());
		} else {
			return null;
		}
	}

	public static Boolean convertToBoolean(String value) throws Exception {
		if (value == null) {
			return null;
		}
		value = value.toLowerCase();
		if ("true".equals(value)) {
			return Boolean.TRUE;
		} else if ("false".equals(value)) {
			return Boolean.FALSE;
		} else if ("1".equals(value)) {
			return Boolean.TRUE;
		} else if ("0".equals(value)) {
			return Boolean.FALSE;
		} else if ("yes".equals(value)) {
			return Boolean.TRUE;
		} else if ("y".equals(value)) {
			return Boolean.TRUE;
		} else if ("sí".equals(value)) {
			return Boolean.TRUE;
		} else if ("да".equals(value)) {
			return Boolean.TRUE;
		} else if ("no".equals(value)) {
			return Boolean.FALSE;
		} else if ("нет".equals(value)) {
			return Boolean.FALSE;
		} else if ("n".equals(value)) {
			return Boolean.FALSE;
		} else if ("ja".equals(value)) {
			return Boolean.TRUE;
		} else if ("j".equals(value)) {
			return Boolean.TRUE;
		} else if ("nein".equals(value)) {
			return Boolean.FALSE;
		} else if ("oui".equals(value)) {
			return Boolean.TRUE;
		} else if ("non".equals(value)) {
			return Boolean.FALSE;
		} else if ("ok".equals(value)) {
			return Boolean.TRUE;
		} else if ("x".equals(value)) {
			return Boolean.TRUE;
		} else if (value != null) {
			throw new Exception("Value: " + value + " cannot be parsed to a boolean");
		} else {
			return null;
		}
	}

	public static Double convertToDouble(String value, String locale) throws Exception {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return getNumberFormat(locale).parse(value.trim()).doubleValue();
	}

	public static Integer convertToInteger(String value, String locale) throws Exception {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return getNumberFormat(locale).parse(value.trim()).intValue();
	}
	
	public static Float convertToFloat(String value, String locale) throws Exception {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return getNumberFormat(locale).parse(value.trim()).floatValue();
	}

	public static Long convertToLong(String value, String locale) throws Exception {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return getNumberFormat(locale).parse(value.trim()).longValue();
	}

	public static Short convertToShort(String value, String locale) throws Exception {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return getNumberFormat(locale).parse(value.trim()).shortValue();
	}

	public static BigDecimal convertToBigDecimal(String value) throws Exception {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return new BigDecimal(value.trim());
		} catch (RuntimeException e) {
			throw new Exception("convertToBigDecimal:" + value + " failed:" + e.getMessage(), e);
		}
	}

}
