package com.benayn.ustyle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.benayn.ustyle.behavior.StructBehavior;
import com.benayn.ustyle.behavior.ValueBehavior;
import com.benayn.ustyle.inner.Options;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public final class JsonW {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(JsonW.class);

	/**
	 * Returns a new {@link JsonW} instance
	 * 
	 * @param target
	 * @return
	 */
	public static JsonW of(Object target) {
		return new JsonW(target);
	}
	
	/**
	 * Converts given target as a JSON string
	 * 
	 * @param target
	 * @return
	 */
	public static String toJson(Object target) {
		return of(target).asJson();
	}
	
	/**
	 * Format the given JSON as an easy-to-read string
	 * 
	 * @param json
	 * @return
	 */
	public static String fmtJson(String json) {
		return of(json).readable().asJson();
	}
	
	/**
	 * Writes the delegate target as JSON string to the given output stream
	 * 
	 * @param output
	 * @throws IOException
	 */
	public void write(final OutputStream output) throws IOException {
		write(output, Charsets.UTF_8);
	}
	
	/**
	 * Writes the delegate target as JSON string to the given output stream and {@link Charset}
	 * 
	 * @param output
	 * @throws IOException
	 */
	public void write(final OutputStream output, Charset charset) throws IOException {
		output.write(asJson().getBytes(charset));
	}
	
	/**
	 * Writes the delegate target as JSON string to the given writer
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public void write(final Writer writer) throws IOException {
		write(writer, Charsets.UTF_8);
	}
	
	/**
	 * Writes the delegate target as JSON string to the given writer and {@link Charset}
	 * 
	 * @param writer
     * @param charset
	 * @throws IOException
	 */
	public void write(final Writer writer, Charset charset) throws IOException {
		writer.write(new String(asJson().getBytes(charset)));
	}
	
	/**
	 * Converts delegate target as a JSON string
	 * 
	 * @return
	 */
	public String asJson() {
		if (this.readabilityO.isPresent()) {
			return doFmt((this.delegate instanceof String) ? ((String) this.delegate) : intlWriting().get().toString());
		}
		
		return intlWriting().get().toString();
	}
	
	/**
	 * Converts delegate target as an easy-to-read JSON string
	 * 
	 * @return
	 */
	public ReadableOptions readable() {
		if (this.readabilityO.isPresent()) {
			return this.readabilityO.get();
		}
		
		return (this.readabilityO = Optional.of(new ReadableOptions(this))).get();
	}
	
	/**
	 * 
	 */
	public class ReadableOptions extends Options<JsonW, ReadableOptions> {
		
		boolean justifyingL = false;
		String fillStringUnit = Strings.repeat(Strs.WHITE_SPACE, 3);
		
		private ReadableOptions(JsonW jsonW) {
			this.reference(jsonW, this);
		}

		/**
		 * Sets the JSON format fill unit
		 * 
		 * @param fillStringUnit
		 * @return
		 */
		public ReadableOptions fill(String fillStringUnit) {
			this.fillStringUnit = fillStringUnit;
			return THIS;
		}
		
		/**
		 * Sets the JSON format fill unit
		 * 
		 * @param fillStringUnit
		 * @return
		 */
		public JsonW fills(String fillStringUnit) {
			return fill(fillStringUnit).outerRef;
		}
		
		/**
		 * Sets the JSON format justifying left
		 * 
		 * @return
		 */
		public ReadableOptions align() {
			this.justifyingL = true;
			return THIS;
		}
		
		/**
		 * Sets the JSON format justifying left
		 * 
		 * @return
		 */
		public JsonW aligns() {
			return align().outerRef;
		}
		
		/**
		 * @see JsonW#asJson()
		 * 
		 * @return
		 */
		public String asJson() {
			return outerRef.asJson();
		}
		
	}
	
	/**
	 * Sets the date time format style with given date time style
	 * 
	 * @param datetimeStyle
	 * @return
	 */
	public JsonW dateFmt(String datetimeStyle) {
		dateStyle = Optional.of(DateStyle.from(datetimeStyle));
		return this;
	}
	
	/**
	 * Sets the date time format style with given {@link DateStyle}
	 * 
	 * @param datetimeStyle
	 * @return
	 */
	public JsonW dateFmt(DateStyle datetimeStyle) {
		dateStyle = Optional.of(datetimeStyle);
		return this;
	}
	
	/**
	 * 
	 */
	private JsonW(Object target) {
		this.delegate = target;
	}
	
	/**
	 * 
	 */
	private class JsonWriteStructBehavior extends StructBehavior<StringBuilder> {
		private StringBuilder strB;
		public JsonWriteStructBehavior(Object delegate) { super(delegate); strB = new StringBuilder(); }
		@Override protected StringBuilder booleanIf() { return strB.append(((Boolean) this.delegate).booleanValue()); }
		@Override protected StringBuilder byteIf() { return strB.append(((Byte) this.delegate).byteValue()); }
		@Override protected StringBuilder characterIf() { 
			return strB.append(quotes).append(((Character) this.delegate).charValue()).append(quotes); }
		@Override protected StringBuilder doubleIf() { return strB.append(((Double) this.delegate).doubleValue()); }
		@Override protected StringBuilder floatIf() { return strB.append(((Float) this.delegate).floatValue()); }
		@Override protected StringBuilder integerIf() { return strB.append(((Integer) this.delegate).intValue()); }
		@Override protected StringBuilder longIf() { return strB.append(((Long) this.delegate).longValue()); }
		@Override protected StringBuilder shortIf() { return strB.append(((Short) this.delegate).shortValue()); }
		@Override protected StringBuilder nullIf() { return strB.append("null"); }
		@Override protected StringBuilder noneMatched() { return null; }
	}

	/**
	 * 
	 */
	private class JsonWriteValueBehaivor extends ValueBehavior<StringBuilder> {
		private StringBuilder strB = null;
		public JsonWriteValueBehaivor(Object delegate) {
			super(delegate);
			strB = new StringBuilder();
		}

		@Override protected <T> StringBuilder classIf(Class<T> resolvedP) {
			return strB.append(resolvedP.getName());
		}

		@Override protected StringBuilder primitiveIf() {
			return strB.append(this.delegate);
		}

		@Override protected StringBuilder eightWrapIf() {
			return new JsonWriteStructBehavior(this.delegate).doDetect();
		}

		@Override protected StringBuilder dateIf(Date resolvedP) {
			if (dateStyle.isPresent()) {
				strB.append(quotes).append(Dater.of(resolvedP).asText(dateStyle.get())).append(quotes);
			} else {
				strB.append(resolvedP.getTime());
			}
			return strB;
		}

		@Override protected StringBuilder stringIf(String resolvedP) {
			return asJsonUtf8String((String) this.delegate, strB);
		}

		@Override protected StringBuilder enumif(Enum<?> resolvedP) {
			return strB.append(quotes).append(resolvedP).append(quotes);
		}

		@Override protected <T> StringBuilder arrayIf(T[] resolvedP, boolean isPrimitive) {
			int no = 0;
			strB.append(arrayL);
			for (T t : resolvedP) {
				strB.append(asStrBuild(t));
				if (++no < resolvedP.length) {
					strB.append(comma);
				}
			}
			strB.append(arrayR);
			return strB;
		}

		@Override protected StringBuilder bigDecimalIf(BigDecimal resolvedP) {
			return strB.append(resolvedP.toString());
		}

		@Override protected StringBuilder bigIntegerIf(BigInteger resolvedP) {
			return strB.append(resolvedP.toString());
		}

		@Override protected <K, V> StringBuilder mapIf(Map<K, V> resolvedP) {
			int no = 0;
			strB.append(objL);
			for (K k : resolvedP.keySet()) {
				V v = resolvedP.get(k);
				strB.append(asStrBuild(k, true)).append(colon).append(asStrBuild(v));
				if (++no < resolvedP.size()) {
					strB.append(comma);
				}
			}
			strB.append(objR);
			return strB;
		}

		@Override protected <T> StringBuilder setIf(Set<T> resolvedP) {
			int no = 0;
			strB.append(arrayL);
			for (T t : resolvedP) {
				strB.append(asStrBuild(t));
				if (++no < resolvedP.size()) {
					strB.append(comma);
				}
			}
			strB.append(arrayR);
			return strB;
		}

		@Override protected <T> StringBuilder listIf(List<T> resolvedP) {
			int no = 0;
			strB.append(arrayL);
			for (T t : resolvedP) {
				strB.append(asStrBuild(t));
				if (++no < resolvedP.size()) {
					strB.append(comma);
				}
			}
			strB.append(arrayR);
			return strB;
		}

		@Override protected StringBuilder beanIf() {
			int no = 0;
			Map<String, Object> props = Reflecter.from(this.delegate).asMap();
			strB.append(objL);
			for (String k : props.keySet()) {
				Object v = props.get(k);
				strB.append(asStrBuild(k, true)).append(colon).append(asStrBuild(v));
				if (++no < props.size()) {
					strB.append(comma);
				}
			}
			strB.append(objR);
			return strB;
		}

		@Override protected StringBuilder nullIf() {
			return strB.append("null");
		}
		
	}
	
	private StringBuilder asStrBuild(Object obj, boolean wrapWithQuotes) {
		StringBuilder strB = asStrBuild(obj);
		if (strB.charAt(0) != quotes) {
			strB.insert(0, quotes).append(quotes);
		}
		return strB;
	}
	
	private StringBuilder asStrBuild(Object obj) {
		return new JsonWriteValueBehaivor(obj).doDetect();
	}
	
	private String doFmt(String fmtTgt) {
		int fixedLenth = 0;
		List<String> tokens = toTokens(fmtTgt);
		boolean justifyingL = this.readabilityO.get().justifyingL;
		String fillStringUnit = this.readabilityO.get().fillStringUnit;
		
		if (justifyingL) {
			for (int i = 0; i < tokens.size(); i++) {
				int length = tokens.get(i).getBytes().length;
				if (length > fixedLenth && i < tokens.size() - 1 && tokens.get(i + 1).equals(symbolS(colon))) {
					fixedLenth = length;
				}
			}
		}

		int count = 0;
		StringBuilder strB = new StringBuilder();

		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (token.equals(",")) {
				strB.append(token);
				doJsonFmtFill(strB, count, fillStringUnit);
				continue;
			}

			if (token.equals(":")) {
				strB.append(" ").append(token).append(" ");
				continue;
			}

			if (token.equals("{")) {
				String nextToken = tokens.get(i + 1);
				if (nextToken.equals("}")) {
					i++;
					strB.append("{ }");
				} else {
					count++;
					strB.append(token);
					doJsonFmtFill(strB, count, fillStringUnit);
				}
				continue;
			}
			if (token.equals("}")) {
				count--;
				doJsonFmtFill(strB, count, fillStringUnit);
				strB.append(token);
				continue;
			}
			if (token.equals("[")) {
				String nextToken = tokens.get(i + 1);
				if (nextToken.equals("]")) {
					i++;
					strB.append("[ ]");
				} else {
					count++;
					strB.append(token);
					doJsonFmtFill(strB, count, fillStringUnit);
				}
				continue;
			}
			if (token.equals("]")) {
				count--;
				doJsonFmtFill(strB, count, fillStringUnit);
				strB.append(token);
				continue;
			}

			strB.append(token);
			
			if (justifyingL) {
				if (i < tokens.size() - 1 && tokens.get(i + 1).equals(":")) {
					int fillLength = fixedLenth - token.getBytes().length;
					if (fillLength > 0) {
						strB.append(Strings.repeat(Strs.WHITE_SPACE, fillLength));
					}
				}
			}
		}

		return strB.toString();
	}
	
	private Optional<StringBuilder> intlWriting() {
		if (this.json.isPresent()) {
			return json;
		}
		
		return (this.json = Optional.of(asStrBuild(this.delegate)));
	}
	
	private static List<String> toTokens(String json) {
		String jsonTemp = json; 
		List<String> tokens = Lists.newArrayList();
		
        while (jsonTemp.length() > 0) { 
        	String token = nextToken(jsonTemp); 
        	jsonTemp = jsonTemp.substring(token.length()); 
        	tokens.add(token.trim()); 
        }
        
        return tokens;
	}
	
	private static String nextToken(String json) {
		boolean insideQuotes = false;
		StringBuilder strB = new StringBuilder();
		
		while (json.length() > 0) {
			String token = json.substring(0, 1);
			json = json.substring(1);

			if (!insideQuotes && (token.equals(symbolS(colon)) || token.equals(symbolS(objL)) || token.equals(symbolS(objR))  
                    || token.equals(symbolS(arrayL)) || token.equals(symbolS(arrayR)) 
                    || token.equals(symbolS(comma)))) {
				if (strB.toString().trim().length() == 0) {
					strB.append(token);
				}

				break;
			}

			if (token.equals("\\")) {
				strB.append(token).append(json.substring(0, 1));
				json = json.substring(1);
				continue;
			}
			
			if (token.equals("\"")) {
				strB.append(token);
				if (insideQuotes) { break; } else { insideQuotes = true; continue; }
			}
			
			strB.append(token);
		}
		
		return strB.toString();
	}
	
	private static final char objL = '{';
	private static final char objR = '}';
	private static final char arrayL = '[';
	private static final char arrayR = ']';
	private static final char quotes = '"';
	private static final char colon = ':';
	private static final char slash = '\\';
	private static final char comma = ',';
	private static String symbolS(Character ch) {
		return ch.toString();
	}

	private static void doJsonFmtFill(StringBuilder buf, int count, String fillStringUnit) {
		buf.append("\n");
		for (int i = 0; i < count; i++) {
			buf.append(fillStringUnit);
		}
	}
	
	private static StringBuilder asJsonUtf8String(String s, StringBuilder strB) {
		strB.append('\"');
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c < ' ') { // Anything less than ASCII space, write either in \\u00xx form, or the special \t, \n, etc. form
				if (c == '\b') {
					strB.append("\\b");
				} else if (c == '\t') {
					strB.append("\\t");
				} else if (c == '\n') {
					strB.append("\\n");
				} else if (c == '\f') {
					strB.append("\\f");
				} else if (c == '\r') {
					strB.append("\\r");
				} else {
					String hex = Integer.toHexString(c);
					strB.append("\\u");
					int pad = 4 - hex.length();
					for (int k = 0; k < pad; k++) {
						strB.append('0');
					}
					strB.append(hex);
				}
			} else if (c == slash || c == quotes) {
				strB.append(slash);
				strB.append(c);
			} else { // Anything else - write in UTF-8 form (multi-byte encoded) (OutputStreamWriter is UTF-8)
				strB.append(c);
			}
		}
		return strB.append('\"');
	}
	
	private Optional<ReadableOptions> readabilityO = Optional.absent();
	private Optional<DateStyle> dateStyle = Optional.absent();
	private Optional<StringBuilder> json = Optional.absent();
	private Object delegate;
}
