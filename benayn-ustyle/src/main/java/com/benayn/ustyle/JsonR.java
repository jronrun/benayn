package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Charsets;
import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;

public final class JsonR {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(JsonR.class);
	
	/**
	 * Returns a new {@link JsonR} instance
	 * 
	 * @param target
	 * @return
	 */
	public static JsonR of(String target) {
		return new JsonR(checkNotNull(target));
	}
	
	/**
	 * 
	 */
	private static final Decision<Field> JSON_READ_DECISION = new Decision<Field>() {
		
		@Override public boolean apply(Field input) {
			return (null != input) && (input.getType().isArray() 
					|| input.getType().isEnum() 
					|| Collection.class.isAssignableFrom(input.getType())
					|| Map.class.isAssignableFrom(input.getType()));
		}
	};
	
	/**
	 * Add the JSON exchange function to the given {@link Reflecter} instance
	 * 
	 * @param target
	 * @return
	 */
	public static <T> Reflecter<T> addJsonExchangeFunc(Reflecter<T> target) {
	    return checkNotNull(target).exchWithField(JSON_READ_FUNC, JSON_READ_DECISION);
	}
	
	/**
	 * Returns the delegate JSON string as a given target
	 * 
	 * @param target
	 * @return
	 */
	public <T> T asObject(Object target) {
		Reflecter<Object> ref = addJsonExchangeFunc(Reflecter.from(target));
		
		if (!this.mappingFuncs.isEmpty()) {
			ref.setExchangeFuncs(this.mappingFuncs);
		}
		
		return ref.populate(noneNullMap()).get();
	}
	
	/**
	 * Mapping property with given value mapping function
	 * 
	 * @param property
	 * @param mappingFunc
	 * @return
	 */
	public <I, O> JsonR valMapping(String property, Function<I, O> mappingFunc) {
		this.mappingFuncs.put(property, mappingFunc);
		return this;
	}
	
	/**
	 * Returns the delegate JSON string as a {@link Gather}
	 * 
	 * @return
	 */
	public Gather<Object> gather() {
		return Gather.from(list());
	}
	
	/**
	 * Returns the delegate JSON string as a {@link List}
	 * 
	 * @return
	 */
	public List<Object> list() {
		Object[] vals = (Object[]) noneNullMap().get(itemsF);
		if (null != vals) {
			return Lists.newArrayList(vals);
		}
		
		return null;
	}
	
	/**
	 * Returns the delegate JSON string as a {@link Mapper}
	 * 
	 * @return
	 */
	public Mapper<String, Object> mapper() {
		return Mapper.from(noneNullMap());
	}
	
	/**
	 * Returns the delegate JSON string as a deep look and tier key {@link HashMap}
	 * 
	 * @see Mapper#deepLook()
	 * @see Mapper#tierKey()
	 * @return
	 */
	public Map<String, Object> deepTierMap() {
	    return mapper().deepLook().tierKey().map();
	}
	
	/**
	 * Returns the delegate JSON string as a {@link HashMap} or null
	 * 
	 * @return
	 */
	public Map<String, Object> map() {
		return intlMapping(false).get();
	}
	
	/**
	 * Returns the delegate JSON string as a {@link HashMap} or empty instance
	 * 
	 * @return
	 */
	public Map<String, Object> noneNullMap() {
		return intlMapping(true).get();
	}
	
	/**
	 * Checks if the delegate JSON string is valid
	 * 
	 * @return
	 */
	public boolean isValid() {
		return null != intlMapping(false);
	}
	
	/**
     * 
     */
    private static final Function<Pair<Field,Object>, Object> JSON_READ_FUNC = new Function<Pair<Field,Object>, Object>() {
        
        @SuppressWarnings({ "unchecked", "rawtypes" }) @Override public Object apply(Pair<Field, Object> input) {
            if (null == input) { return null; }
            
            Field field = input.getL();
            Object val = input.getR();
            if (null == val) { return val; }
            
            //field byte array, val string
            if (field.getType().isArray() 
                    && (Byte.class == Primitives.wrap(field.getType().getComponentType())) && (val instanceof String)) {
                if (Objects2.isPrimitive(field.getType().getComponentType())) {
                    return val.toString().getBytes(Charsets.UTF_8);
                }
                
                return Arrays2.wrap(val.toString().getBytes(Charsets.UTF_8));
            }
            //filed array, val array
            else if (field.getType().isArray() && Objects2.is8Type(field.getType().getComponentType()) && val.getClass().isArray()) {
                //field primitive array
                if (Objects2.isPrimitive(field.getType().getComponentType())) {
                    return Arrays2.unwraps(Arrays2.convert((Object[]) val, Primitives.wrap(field.getType().getComponentType())));
                } 
                
                return Arrays2.convert((Object[]) val, Primitives.wrap(field.getType().getComponentType()));
            }
            //field list, val array
            else if (List.class.isAssignableFrom(field.getType())) {
                return Resolves.get(field, val);
            }
            //field set, val array
            else if (Set.class.isAssignableFrom(field.getType())) {
                return Resolves.get(field, val);
            }
            //field map
            else if (Map.class.isAssignableFrom(field.getType())) {
                return Resolves.get(field, val);
            }
            //field enum, val string
            else if (field.getType().isEnum() && (val instanceof String)) {
//              return Enums.valueOfFunction((Class<Enum>) field.getType()).apply(val.toString().toUpperCase());
                return Enums.stringConverter((Class<Enum>) field.getType()).convert(val.toString().toUpperCase());
            }
            
            return val;
        }
    };
	
	/**
	 * 
	 */
	private JsonR(String target) {
		this.delegate = new AsyncPushbackReader(
				new BufferedReader(
				new InputStreamReader(
				new ByteArrayInputStream(target.getBytes(Charsets.UTF_8)), Charsets.UTF_8)));
	}
	
	private static final char objL = '{';
	private static final char objR = '}';
	private static final char arrayL = '[';
	private static final char arrayR = ']';
	private static final char quotes = '"';
	private static final char colon = ':';
	private static final char slash = '\\';
	private static final String itemsF = "$items";
	
	/**
	 * 
	 */
	private Object readValue() throws IOException {
		int ch = this.delegate.read();

		if (ch == quotes) {
			return readString();
		}
		if (isDigit(ch) || ch == '-') {
			return readNumber(ch);
		}
		if (ch == objL) {
			this.delegate.unread(objL);
			return mappingBuild();
		}
		if (ch == 't' || ch == 'T') {
			this.delegate.unread(ch);
			readToken("true");
			return Boolean.TRUE;
		}
		if (ch == 'f' || ch == 'F') {
			this.delegate.unread(ch);
			readToken("false");
			return Boolean.FALSE;
		}
		if (ch == 'n' || ch == 'N') {
			this.delegate.unread(ch);
			readToken("null");
			return null;
		}
		if (ch == arrayL) {
			return readArray();
		}
		if (ch == arrayR) {
			this.delegate.unread(arrayR);
			return null;
		}
		if (ch == -1) {
			throw new IOException("EOF reached prematurely");
		}
		
		throw new IOException("Unknown value type at position " + this.delegate.getPos());
	}

	private Optional<Map<String, Object>> intlMapping(boolean noneNull) {
		if (this.mapping.isPresent()) {
			return this.mapping;
		}
		
		try {
			return (this.mapping = Optional.of(mappingBuild()));
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			close();
		}
		
		if (noneNull) {
			Map<String, Object> m = Maps.newHashMap();
			return (this.mapping = Optional.of(m));
		}
		
		return null;
	}
	
	private Object readArray() throws IOException {
		List<Object> l = Lists.newArrayList();
		while (true) {
			skipWhitespace();
			Object o = readValue();
			if (null != o) {
				l.add(o);
			}
			int c = skipWhitespaceRead();

			if (c == arrayR) {
				break;
			}
			if (c != ',') {
				throw new IOException("Expected ',' or ']' inside array at position " + this.delegate.getPos());
			}
		}

		return l.toArray();
	}

	/**
	 * Return the specified token from the reader. If it is not found, throw an
	 * IOException indicating that. Converting to c to (char) c is acceptable
	 * because the 'tokens' allowed in a JSON input stream (true, false, null)
	 * are all ASCII.
	 */
	private String readToken(String token) throws IOException {
		int len = token.length();

		for (int i = 0; i < len; i++) {
			int c = this.delegate.read();
			if (c == -1) {
				throw new IOException("EOF reached while reading token: " + token);
			}
			c = Character.toLowerCase((char) c);
			int loTokenChar = token.charAt(i);

			if (loTokenChar != c) {
				throw new IOException("Expected token: " + token + " at position " + this.delegate.getPos());
			}
		}

		return token;
	}

	private Number readNumber(int ch) throws IOException {
		final AsyncPushbackReader in = this.delegate;
		final char[] numBuf = _numBuf;
		numBuf[0] = (char) ch;
		int len = 1;
		boolean isFloat = false;

		try {
			while (true) {
				ch = in.read();
				if ((ch >= '0' && ch <= '9') || ch == '-' || ch == '+') {
					numBuf[len++] = (char) ch;
				} else if (ch == '.' || ch == 'e' || ch == 'E') {
					numBuf[len++] = (char) ch;
					isFloat = true;
				} else if (ch == -1) {
					throw new IOException("Reached EOF while reading number at position " + in.getPos());
				} else {
					in.unread(ch);
					break;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IOException("Too many digits in number at position " + in.getPos());
		}

		if (isFloat) { // Floating point number needed
			String num = new String(numBuf, 0, len);
			try {
				return Double.parseDouble(num);
			} catch (NumberFormatException e) {
				throw new IOException("Invalid floating point number at position " + in.getPos() + ", number: " + num);
			}
		}
		
		boolean isNeg = numBuf[0] == '-';
		long n = 0;
		for (int i = (isNeg ? 1 : 0); i < len; i++) {
			n = (numBuf[i] - '0') + n * 10;
		}
		
		return isNeg ? -n : n;
	}
	
	private void close() {
		try {
			if (null != this.delegate) {
				this.delegate.close();
			}
		} catch (IOException ignored) {
		}
	}
	
	private String readString() throws IOException {
		final StringBuilder strBuf = _strBuf;
		strBuf.setLength(0);
		StringBuilder hex = new StringBuilder();
		boolean done = false;
		final int STATE_STRING_START = 0;
		final int STATE_STRING_SLASH = 1;
		final int STATE_HEX_DIGITS = 2;
		final int STATE_FILED_ARRAY = 3;
		final int STATE_FILED_OBJ = 4;
		int state = STATE_STRING_START;
		
		int arraySymbol = 0;
		int objSymbol = 0;

		while (!done) {
			int ch = this.delegate.read();
			if (ch == -1) {
				throw new IOException("EOF reached while reading JSON string");
			}

			switch (state) {
			case STATE_STRING_START:
				if (ch == slash) {
					state = STATE_STRING_SLASH;
				} else if (ch == quotes) {
					done = true;
				} 
				//field JSON array
				else if (ch == arrayL) {
				    state = STATE_FILED_ARRAY;
				    strBuf.append(arrayL);
				    ++arraySymbol;
				} 
				//field JSON obj
				else if (ch == objL) {
				    state = STATE_FILED_OBJ;
				    strBuf.append(objL);
				    ++objSymbol;
				}
				else {
					strBuf.append(toChars(ch));
				}
				break;
				
			case STATE_FILED_OBJ:
			    if (ch == objL) {
                    ++objSymbol;
                } else if (ch == objR) {
                    --objSymbol;
                    if (objSymbol == 0) {
                        state = STATE_STRING_START;
                    }
                }
                strBuf.append(toChars(ch));
			    break;
				
			case STATE_FILED_ARRAY:
			    if (ch == arrayL) {
			        ++arraySymbol;
			    } else if (ch == arrayR) {
			        --arraySymbol;
			        if (arraySymbol == 0) {
			            state = STATE_STRING_START;
			        }
			    }
			    strBuf.append(toChars(ch));
			    break;

			case STATE_STRING_SLASH:
				if (ch == 'n') {
					strBuf.append(Strs.LF);
				} else if (ch == 'r') {
					strBuf.append(Strs.CR);
				} else if (ch == 't') {
					strBuf.append(Strs.TAB);
				} else if (ch == 'f') {
					strBuf.append('\f');
				} else if (ch == 'b') {
					strBuf.append('\b');
				} else if (ch == slash) {
					strBuf.append(slash);
				} else if (ch == '/') {
					strBuf.append('/');
				} else if (ch == quotes) {
					strBuf.append(quotes);
				} else if (ch == '\'') {
					strBuf.append('\'');
				} else if (ch == 'u') {
					state = STATE_HEX_DIGITS;
					hex.setLength(0);
					break;
				} else {
					throw new IOException("Invalid character escape sequence specified at position " + this.delegate.getPos());
				}
				state = STATE_STRING_START;
				break;

			case STATE_HEX_DIGITS:
				if (isHexDigit(ch)) {
					hex.append((char) ch);
					if (hex.length() == 4) {
						int value = Integer.parseInt(hex.toString(), 16);
						strBuf.append(valueOf((char) value));
						state = STATE_STRING_START;
					}
				} else {
					throw new IOException("Expected hexadecimal digits at position " + this.delegate.getPos());
				}
				break;
			}
		}

		return strBuf.toString();
	}
	
	private static boolean isHexDigit(int ch) {
		return isDigit(ch)
				|| ch == 'a' || ch == 'A' 
				|| ch == 'b' || ch == 'B' 
				|| ch == 'c' || ch == 'C' 
				|| ch == 'd' || ch == 'D' 
				|| ch == 'e' || ch == 'E' 
				|| ch == 'f' || ch == 'F';
	}
	
	private static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }
	
	/**
     * This is a performance optimization.  The lowest 128 characters are re-used.
     *
     * @param c char to match to a Character.
     * @return a Character that matches the passed in char.  If the valuye is
     *         less than 127, then the same Character instances are re-used.
     */
    private static Character valueOf(char c)
    {
        return c <= 127 ? _charCache[(int) c] : c;
    }

    public static final int MAX_CODE_POINT = 0x10ffff;
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;
    public static final char MIN_LOW_SURROGATE = '\uDC00';
    public static final char MIN_HIGH_SURROGATE = '\uD800';

    private static char[] toChars(int codePoint)
    {
        if (codePoint < 0 || codePoint > MAX_CODE_POINT)
        {    // int UTF-8 char must be in range
            throw new IllegalArgumentException("value ' + codePoint + ' outside UTF-8 range");
        }

        if (codePoint < MIN_SUPPLEMENTARY_CODE_POINT)
        {    // if the int character fits in two bytes...
            return new char[]{(char) codePoint};
        }

        char[] result = new char[2];
        int offset = codePoint - MIN_SUPPLEMENTARY_CODE_POINT;
        result[1] = (char) ((offset & 0x3ff) + MIN_LOW_SURROGATE);
        result[0] = (char) ((offset >>> 10) + MIN_HIGH_SURROGATE);
        return result;
    }
    
	private int skipWhitespaceRead() throws IOException {
		final AsyncPushbackReader in = this.delegate;
		int c = in.read();
		while (Strs.isWhitespace(c)) {
			c = in.read();
		}

		return c;
	}

	private void skipWhitespace() throws IOException {
		int c = skipWhitespaceRead();
		this.delegate.unread(c);
	}
	
	/**
	 * 
	 */
	private static class AsyncPushbackReader extends FilterReader {

		private final int[] _buf;
		private int _idx;
		private long _pos;

		private AsyncPushbackReader(Reader reader, int size) {
			super(reader);
			checkArgument(size > 0, "size <= 0");
			_buf = new int[size];
			_idx = size;
		}

		private AsyncPushbackReader(Reader r) {
			this(r, 1);
		}

		public long getPos() {
			return _pos;
		}

		@Override public int read() throws IOException {
			_pos++;
			if (_idx < _buf.length) {
				return _buf[_idx++];
			}
			return super.read();
		}

		public void unread(int c) throws IOException {
			if (_idx == 0) {
				throw new IOException("AsyncPushback buffer overflow: buffer size (" + _buf.length + "), position = " + _pos);
			}
			_pos--;
			_buf[--_idx] = c;
		}

		@Override public void close() throws IOException {
			super.close();
			_pos = 0;
		}

	}
	
	private Map<String, Object> mappingBuild() throws IOException {
		boolean done = false, objectR = false;
		//'S' read start object, 'F' field, 'V' value, 'P' post
		char state = 'S';
		String field = null;
		Map<String, Object> map = Maps.newHashMap();
		final AsyncPushbackReader in = this.delegate;
		
		while (!done) {
			int ch;
			switch (state) {
			case 'S':
				ch = skipWhitespaceRead();
				if (objL == ch) {
					objectR = true;
					ch = skipWhitespaceRead();
					if (objR == ch) {
						return null;
					}
					in.unread(ch);
					state = 'F';
				}
				else if (arrayL == ch) {
					in.unread(arrayL);
					state = 'V';
				}
				else {
					throw new IOException("Input is invalid JSON; does not start with '{' or '[', ch=" + ch);
				}
				break;
			case 'F':
				ch = skipWhitespaceRead();
				if (quotes == ch) {
					field = readString();
					ch = skipWhitespaceRead();
					if (colon != ch) {
						throw new IOException("Expected ':' between string field and value at position " + in.getPos());
					}
					skipWhitespace();
					state = 'V';
				} else {
					throw new IOException("Expected quote at position " + in.getPos());
				}
				break;
			case 'V':
				if (null == field) {
					field = itemsF;
				}
				map.put(field, readValue());
				state = 'P';
				break;
			case 'P':
                ch = skipWhitespaceRead();
                if (ch == -1 && objectR) {
                    throw new IOException("EOF reached before closing '}'");
                }
                if (ch == objR || ch == -1) {
                    done = true;
                }
                else if (ch == ',') {
                    state = 'F';
                }
                else {
                    throw new IOException("Object not ended with '}' or ']' at position " + in.getPos());
                }
                break;
			}
		}
		
		return map;
	}
	
	private final char[] _numBuf = new char[256];
	private final StringBuilder _strBuf = new StringBuilder();
	
	private static final Byte[] _byteCache = new Byte[256];
	private static final Character[] _charCache = new Character[128];
	
	static {
        for (int i = 0; i < _charCache.length; i++) {
            _charCache[i] = (char) i;
        }

        for (int i = 0; i < _byteCache.length; i++) {
            _byteCache[i] = (byte) (i - 128);
        }
	}
	
	private AsyncPushbackReader delegate;
	private Optional<Map<String, Object>> mapping = Optional.absent(); 
	private Map<String, Function<?, ?>> mappingFuncs = Maps.newHashMap();

}
