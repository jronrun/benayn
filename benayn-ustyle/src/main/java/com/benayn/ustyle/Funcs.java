/**
 * 
 */
package com.benayn.ustyle;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.benayn.ustyle.string.Strs;
import com.google.common.base.Function;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Longs;

/**
 *
 */
public class Funcs {
	
	/**
	 * Parses the argument as a string
	 */
	public static final Function<Object, String> TO_STRING = new Function<Object, String>() {
		
		@Override public String apply(Object input) {
			if (null == input) { return null; }
			if (input instanceof String) { return (String) input; }
			if (Objects2.is8Type(input.getClass())) { return String.valueOf(input); }
			return input.toString();
		}
	};
	
	/**
	 * Parses the argument as a character
	 */
	public static final Function<Object, Character> TO_CHARACTER = new Function<Object, Character>() {

		@Override public Character apply(Object input) {
			if (null == input) { return null; }
			if (input instanceof Character) { return (Character) input; }
			if (input instanceof String 
					&& (((String) input).length() == 1)
					&& Strs.ASCII.matchesAllOf((String) input)) {
				return ((String) input).toCharArray()[0];
			}
			if (Objects2.is8Type(input.getClass())) {
				String c = null;
				if ((c = String.valueOf(input)).length() == 1) {
					return c.toCharArray()[0];
				}
			}
			return null;
		}
	};
	
	/**
	 * Parses the argument as a date
	 */
	public static final Function<Object, Date> TO_DATE = new Function<Object, Date>() {
		
		@Override public Date apply(Object input) {
			if (null == input) { return null; }
			if (input instanceof Date) { return (Date) input; }
			if (input instanceof Long) {
				try { return Dater.of((Long) input).get(); } catch (Exception e) { return null; }
			}
			if (input instanceof String) {
				if (Strs.DIGIT.matchesAllOf((String) input)) {
					try { return Dater.of(Longs.tryParse((String) input)).get(); } catch (Exception e) { }
				}
				try { return Dater.of((String) input).get(); } catch (Exception e) { return null; }
			}
			return null;
		}
	}; 
	
	/**
	 * Parses the argument as a boolean
	 */
	public static final Function<Object, Boolean> TO_BOOLEAN = new Function<Object, Boolean>() {
		@Override public Boolean apply(Object input) {
			if (input instanceof Boolean) { return (Boolean) input; }
			if (null != input && (String.class.isInstance(input)
							&& (((String) input).equalsIgnoreCase(Boolean.TRUE.toString())
									|| ((String) input).equalsIgnoreCase(Boolean.FALSE.toString()) ))) {
				return Boolean.parseBoolean(input.toString());
			}
			
			return null;
		}
	};
	
	/**
	 * Parses the argument as a BigDecimal
	 */
	public static final Function<Object, BigDecimal> TO_BIGDECIMAL = new Function<Object, BigDecimal>() {

		@Override public BigDecimal apply(Object input) {
			if (input instanceof BigDecimal) { return (BigDecimal) input; }
			if (!Objects2.isParseable(input)) return null;
			return BigDecimal.valueOf(Doubles.tryParse(input.toString()));
		}
	};
	
	/**
	 * Parses the argument as a BigInteger
	 */
	public static final Function<Object, BigInteger> TO_BIGINTEGER = new Function<Object, BigInteger>() {
		
		@Override public BigInteger apply(Object input) {
			if (input instanceof BigInteger) { return (BigInteger) input; }
			if (!Objects2.isParseable(input)) return null;
			return BigInteger.valueOf(Longs.tryParse(input.toString()));
		}
	};
	
	/**
	 * Parses the argument as a signed decimal byte
	 */
	public static final Function<Object, Byte> TO_BYTE = new Function<Object, Byte>() {
		@Override public Byte apply(Object input) {
			if (input instanceof Byte) { return (Byte) input; }
			if (!Objects2.isParseable(input)) return null;
			return Byte.parseByte(input.toString());
		}
	};
	
	/**
	 * Parses the argument as a signed decimal short
	 */
	public static final Function<Object, Short> TO_SHORT = new Function<Object, Short>() {
		@Override public Short apply(Object input) {
			if (input instanceof Short) { return (Short) input; }
			if (!Objects2.isParseable(input)) return null;
			return Short.parseShort(input.toString());
		}
	};
	
	/**
	 * Returns a new float initialized to the value represented by the specified String, as performed by the valueOf method of class Float.
	 */
	public static final Function<Object, Float> TO_FLOAT = new Function<Object, Float>() {
		@Override public Float apply(Object input) {
			if (input instanceof Float) { return (Float) input; }
			if (input instanceof String) { return Floats.tryParse((String) input); }
			if (!Objects2.isParseable(input)) return null;
			return Float.parseFloat(input.toString());
		}
	};
	
	/**
	 * Returns a new double initialized to the value represented by the specified String, as performed by the valueOf method of class Double.
	 */
	public static final Function<Object, Double> TO_DOUBLE = new Function<Object, Double>() {
		@Override public Double apply(Object input) {
			if (input instanceof Double) { return (Double) input; }
			if (input instanceof String) { return Doubles.tryParse((String) input); }
			if (!Objects2.isParseable(input)) return null;
			return Double.parseDouble(input.toString());
		}
	};
	
	/**
	 * Parses the argument as a signed decimal integer
	 */
	public static final Function<Object, Integer> TO_INTEGER = new Function<Object, Integer>() {
		@Override public Integer apply(Object input) {
			if (input instanceof Integer) { return (Integer) input; }
			if (!Objects2.isParseable(input)) return null;
			return Integer.parseInt(input.toString());
		}
	};
	
	/**
	 * Parses the argument as a signed decimal long
	 */
	public static final Function<Object, Long> TO_LONG = new Function<Object, Long>() {
		@Override public Long apply(Object input) {
			if (input instanceof Long) { return (Long) input; }
			if (!Objects2.isParseable(input)) return null;
			return Long.parseLong(input.toString());
		}
	};

}
