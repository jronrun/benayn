package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import com.google.common.base.Converter;
import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;

/**
 * JSONer custom Converter usage:
 * 
 * <pre>
 * JSONer jsoner = JSONer.build();
 *       
 * FacadeObject<Item> fo = FacadeObject.wrap(Item.class);
 * fo.populate4Test();
 *       
 * jsoner.register(new JSONer.GenericConverter<String, short[]>() {
 *
 *     @Override 
 *     protected short[] forward(String input) {
 *         Short[] s = JSONer.read(input).asObject(Short[].class);
 *         return Arrays2.unwrap(s);
 *     }
 *
 *     @Override 
 *     protected String backward(short[] input) {
 *         return JSONer.toJson(input);
 * }}, short[].class);
 *      
 * String json = jsoner.update(fo.get()).asJson();
 * Item item = jsoner.update(json).asObject(Item.class);
 *      
 * assertDeepEqual(fo.get(), item);
 * </pre>
 * 
 * https://github.com/jronrun/benayn
 */
public final class JSONer {

    /**
     * Returns a new {@link JSONer} instance
     * 
     * @return
     */
    public static JSONer build() {
        return of(null);
    }
    
    /**
     * Returns a new {@link JSONer} instance with given target
     * 
     * @return
     */
    public static JSONer of(Object target) {
        return new JSONer(target);
    }
    
    /**
     * Returns a new {@link List} instance with given JSON string or <code>null</code>
     * 
     * @see ReadJSON#list()
     * @param target
     * @return
     */
    public static List<Object> readList(String target) {
        return read(target).list();
    }
    
    /**
     * Returns a new {@link Map} instance with given JSON string or <code>null</code>
     * 
     * @see ReadJSON#map()
     * @param target
     * @return
     */
    public static Map<String, Object> readMap(String target) {
        return read(target).map();
    }
    
    /**
     * Returns a new {@link Map} instance with given JSON string
     * 
     * @see ReadJSON#noneNullMap()
     * @param target
     * @return
     */
    public static Map<String, Object> readNoneNullMap(String target) {
        return read(target).noneNullMap();
    }
    
    /**
     * Returns a new deeply look and tier key supports {@link Map} instance with given JSON string
     * 
     * @see ReadJSON#deepTierMap()
     * @param target
     * @return
     */
    public static Map<String, Object> readDeepTierMap(String target) {
        return read(target).deepTierMap();
    }
    
    /**
     * Returns a new {@link ReadJSON} instance with given JSON string
     * 
     * @param target
     * @return
     */
    public static ReadJSON read(String target) {
        return new ReadJSON(checkNotNull(target), null);
    }
    
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
     * Returns a new {@link WriteJSON} instance
     * 
     * @param target
     * @return
     */
    public static WriteJSON write(Object target) {
        return new WriteJSON(target, null);
    }
    
    /**
     * Converts given target as a JSON string
     * 
     * @param target
     * @return
     */
    public static String toJson(Object target) {
        return write(target).asJson();
    }
    
    /**
     * Format the given JSON as an easy-to-read string
     * 
     * @param json
     * @return
     */
    public static String fmtJson(Object json) {
        return write(json).readable().asJson();
    }
    
    /**
     * 
     */
    public static final class WriteJSON {
        
        /**
         * 
         */
        protected static final Log log = Loggers.from("JSONer.WriteJSON");
        
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
                return doFmt((this.delegate instanceof String) 
                        ? ((String) this.delegate) : intlWriting().get().toString());
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
        public class ReadableOptions extends Options<WriteJSON, ReadableOptions> {
            
            boolean justifyingL = false;
            boolean showClassName = false;
            String fillStringUnit = Strings.repeat(Strs.WHITE_SPACE, 3);
            Optional<DateStyle> dateStyle = Optional.absent();
            
            private ReadableOptions(WriteJSON jsonW) {
                this.reference(jsonW, this);
            }
            
            /**
             * Sets if shows the class name, default is false
             * 
             * @return
             */
            public ReadableOptions showClassName() {
                this.showClassName = true;
                return THIS;
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
             * Sets the JSON format justifying left
             * 
             * @return
             */
            public ReadableOptions align() {
                this.justifyingL = true;
                return THIS;
            }
            
            /**
             * Sets the date time format style with given date time style
             * 
             * @param datetimeStyle
             * @return
             */
            public ReadableOptions dateFmt(String datetimeStyle) {
                dateStyle = Optional.of(DateStyle.from(checkNotNull(datetimeStyle)));
                return THIS;
            }
            
            /**
             * Sets the date time format style with given {@link DateStyle}
             * 
             * @param datetimeStyle
             * @return
             */
            public ReadableOptions dateFmt(DateStyle datetimeStyle) {
                dateStyle = Optional.of(datetimeStyle);
                return THIS;
            }
            
            /**
             * @see WriteJSON#asJson()
             * 
             * @return
             */
            public String asJson() {
                return outerRef.asJson();
            }
            
        }
        
        /**
         * 
         */
        private WriteJSON(Object target, JSONer jsoner) {
            this.delegate = target;
            this.jsoner = Optional.fromNullable(jsoner);
        }
        
        //Decides convert or not convert the value with the register Converter
        //e.g: avoid convert nested type
        private boolean convertSwitch = true;
        private void switchConvert() {
        	this.convertSwitch = !convertSwitch;
        }
        
        private <R, W> R convert(Class<?> type, W typeValue) {
            if (!convertSwitch || !jsoner.isPresent()) {
                return null;
            }
            
            Converter<R, W> converter = jsoner.get().getTypeConverter(type);
            return null != converter ? converter.reverse().convert(typeValue) : null;
        }
        
        @SuppressWarnings("unchecked")
        private <R, W> R convert(String property, W propertyValue) {
            if (!convertSwitch || !jsoner.isPresent()) {
                return null;
            }
            
            Converter<R, W> converter = jsoner.get().getConverter(property);
            propertyValue = (W) (propertyValue.getClass().isArray() 
                    ? Arrays2.wraps(propertyValue) : propertyValue);
            return null != converter ? converter.reverse().convert(propertyValue) : null;
        }
        
        /**
         * 
         */
        private class JsonWriteStructBehavior extends StructBehavior<StringBuilder> {
            private StringBuilder strB;
            private Class<?> fieldClass = null;

            public JsonWriteStructBehavior(Object delegate, Class<?> fieldClass) {
                super(delegate);
                strB = new StringBuilder();
                this.fieldClass = fieldClass;
            }
            
            private <T, R> StringBuilder doConvert(Class<?> type, T defaultValue) {
                R result = convert(null != fieldClass ? fieldClass : type, this.delegate);
                return null != result ? strB.append(result) : (null != defaultValue ? strB.append(defaultValue) : null);
            }

            @Override protected StringBuilder booleanIf() {
                return doConvert(Boolean.class, ((Boolean) this.delegate).booleanValue());
            }

            @Override protected StringBuilder byteIf() {
                return doConvert(Byte.class, ((Byte) this.delegate).byteValue());
            }

            @Override protected StringBuilder characterIf() {
                if (null != doConvert(Character.class, null)) {
                    return strB;
                }
                
                return strB.append(quotes).append(((Character) this.delegate).charValue()).append(quotes);
            }

            @Override protected StringBuilder doubleIf() {
                return doConvert(Double.class, ((Double) this.delegate).doubleValue());
            }

            @Override protected StringBuilder floatIf() {
                return doConvert(Float.class, ((Float) this.delegate).floatValue());
            }

            @Override protected StringBuilder integerIf() {
                return doConvert(Integer.class, ((Integer) this.delegate).intValue());
            }

            @Override protected StringBuilder longIf() {
                return doConvert(Long.class, ((Long) this.delegate).longValue());
            }

            @Override protected StringBuilder shortIf() {
                return doConvert(Short.class, ((Short) this.delegate).shortValue());
            }

            @Override protected StringBuilder nullIf() { return strB.append("null"); }
            @Override protected StringBuilder noneMatched() { return null; }
            
        }

        /**
         * 
         */
        private class JsonWriteValueBehaivor extends ValueBehavior<StringBuilder> {
            private StringBuilder strB = null;
            private Class<?> fieldClass = null;
            
            public JsonWriteValueBehaivor(Object delegate, Class<?> fieldClass) {
                super(delegate);
                strB = new StringBuilder();
                this.fieldClass = fieldClass;
            }

            private <T, R> StringBuilder doConvert(Class<?> type, T defaultValue) {
                R result = convert(null != fieldClass ? fieldClass : type, this.delegate);
                return null != result ? strB.append(result) : (null != defaultValue ? strB.append(defaultValue) : null);
            }
            
            @Override protected <T> StringBuilder classIf(Class<T> resolvedP) {
                if (null != doConvert(Class.class, null)) {
                    return strB;
                }
                
                return strB.append(resolvedP.getName());
            }

            @Override protected StringBuilder primitiveIf() {
                return strB.append(this.delegate);
            }

            @Override protected StringBuilder eightWrapIf() {
                return new JsonWriteStructBehavior(this.delegate, fieldClass).doDetect();
            }

            @Override protected StringBuilder dateIf(Date resolvedP) {
                if (null != doConvert(Date.class, null)) {
                    return strB;
                } 
                
                if (readabilityO.isPresent() && readabilityO.get().dateStyle.isPresent()) {
                    strB.append(quotes).append(
                            Dater.of(resolvedP).asText(readabilityO.get().dateStyle.get())).append(quotes);
                } else {
                    strB.append(resolvedP.getTime());
                }
                
                return strB;
            }

            @Override protected StringBuilder stringIf(String resolvedP) {
                if (null != doConvert(String.class, null)) {
                    return strB;
                }
                
                return asJsonUtf8String((String) this.delegate, strB);
            }

            @Override protected StringBuilder enumif(Enum<?> resolvedP) {
                if (null != doConvert(Enum.class, null)) {
                    return strB;
                }
                
                return strB.append(quotes).append(resolvedP).append(quotes);
            }

            @Override protected <T> StringBuilder arrayIf(T[] resolvedP, boolean isPrimitive) {
                if (null != resolvedP) {
                    if (null != doConvert(isPrimitive 
                            ? Arrays2.unwrapArrayType(resolvedP.getClass()) : resolvedP.getClass(), null)) {
                        return strB;
                    }
                }
                
                if (null != doConvert(Object[].class, null)) {
                    return strB;
                }
                
                int no = 0;
                strB.append(arrayL);
                for (T t : resolvedP) {
                	switchConvert();
                    strB.append(asStrBuild(t));
                    switchConvert();
                    if (++no < resolvedP.length) {
                        strB.append(comma);
                    }
                }
                strB.append(arrayR);
                return strB;
            }

            @Override protected StringBuilder bigDecimalIf(BigDecimal resolvedP) {
                if (null != doConvert(BigDecimal.class, null)) {
                    return strB;
                }
                
                return strB.append(resolvedP.toString());
            }

            @Override protected StringBuilder bigIntegerIf(BigInteger resolvedP) {
                if (null != doConvert(BigInteger.class, null)) {
                    return strB;
                }
                
                return strB.append(resolvedP.toString());
            }

            @Override protected <K, V> StringBuilder mapIf(Map<K, V> resolvedP) {
                if (null != doConvert(Map.class, null)) {
                    return strB;
                }
                
                int no = 0;
                strB.append(objL);
                for (K k : resolvedP.keySet()) {
                    V v = resolvedP.get(k);
                    switchConvert();
                    strB.append(asStrBuild(k, true)).append(colon).append(asStrBuild(v));
                    switchConvert();
                    if (++no < resolvedP.size()) {
                        strB.append(comma);
                    }
                }
                strB.append(objR);
                return strB;
            }

            @Override protected <T> StringBuilder setIf(Set<T> resolvedP) {
                if (null != doConvert(Set.class, null)) {
                    return strB;
                }
                
                int no = 0;
                strB.append(arrayL);
                for (T t : resolvedP) {
                	switchConvert();
                    strB.append(asStrBuild(t));
                    switchConvert();
                    if (++no < resolvedP.size()) {
                        strB.append(comma);
                    }
                }
                strB.append(arrayR);
                return strB;
            }

            @Override protected <T> StringBuilder listIf(List<T> resolvedP) {
                if (null != doConvert(List.class, null)) {
                    return strB;
                }
                
                int no = 0;
                strB.append(arrayL);
                for (T t : resolvedP) {
                	switchConvert();
                    strB.append(asStrBuild(t));
                    switchConvert();
                    if (++no < resolvedP.size()) {
                        strB.append(comma);
                    }
                }
                strB.append(arrayR);
                return strB;
            }

            @Override protected StringBuilder beanIf() {
                if (null != doConvert(Object.class, null)) {
                    return strB;
                }
                
                if (this.clazz == Field.class) {
                    strB.append(quotes).append(this.delegate.toString()).append(quotes);
                    return strB;
                }
                
                int no = 0;
                Reflecter<Object> ref = Reflecter.from(this.delegate);
                Map<String, Object> props = ref.asMap();
                strB.append(objL);
                if (readabilityO.isPresent() && readabilityO.get().showClassName) {
                    props.put("class", this.clazz.getName());
                }
                
                Object convertR = null;
                for (String k : props.keySet()) {
                    Object v = props.get(k);
                    
                    strB.append(asStrBuild(k, true)).append(colon)
                        .append(null != (convertR = convert(k, v)) 
                        ? convertR : asStrBuild(v, ref.field(k).getType()));
                    
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
        	return asStrBuild(obj, null);
        }
        
        private StringBuilder asStrBuild(Object obj, Class<?> fieldClass) {
            return new JsonWriteValueBehaivor(obj, fieldClass).doDetect();
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
        private Optional<StringBuilder> json = Optional.absent();
        private Optional<JSONer> jsoner = Optional.absent();
        private Object delegate;
        private WriteJSON() {}
        
    }
    
    /**
     * @see Converter
     */
    public static abstract class GenericConverter<R, W> extends Converter<R, W> {
        
        /**
         * Quotes the given {@link String} target
         * 
         * @param target
         * @return
         */
        protected String quotes(String target) {
            return new StringBuilder()
                .append(WriteJSON.quotes)
                .append(target)
                .append(WriteJSON.quotes).toString();
        }

        @Override protected W doForward(R a) {
            return forward(a);
        }

        @SuppressWarnings("unchecked") @Override protected R doBackward(W b) {
            R r = backward(b);
            
            if (r instanceof String) {
                return (R) quotes((String) r);
            }
            
            return r;
        }
        
        /**
         * Converts JSON to object. 
         * {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, 
         * {@link Float}, {@link Double}, {@link BigInteger}, {@link BigDecimal} read as {@link Number}
         * 
         * @see Converter#doForward(R)
         */
        protected abstract W forward(R input);
        
        /**
         * Converts object to JSON.
         * {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, 
         * {@link Float}, {@link Double}, {@link BigInteger}, {@link BigDecimal} converts as {@link Number}
         * 
         * @see Converter#doBackward(W)
         */
        protected abstract R backward(W input);
        
    }
    
    /**
     * Register a property based JSON {@link GenericConverter} with given properties
     * 
     * @param converter
     * @param property
     * @return
     */
    public <R, W> JSONer register(Converter<R, W> converter, String... properties) {
        for (String property : checkNotNull(properties)) {
            converts.put(property, checkNotNull(converter));
        }
        return this;
    }
    
    /**
     * Register a class based JSON {@link GenericConverter} with given types
     * 
     * @param property
     * @param converter
     * @return
     */
    public <R, W> JSONer register(Converter<R, W> converter, Class<?>... types) {
        for (Class<?> type : checkNotNull(types)) {
            typeConverts.put(type, checkNotNull(converter));
        }
        return this;
    }
    
    /**
     * Unregister a JSON {@link GenericConverter} with given class type
     * 
     * @param converter
     * @return
     */
    public <R, W> JSONer unregister(Class<?> type) {
        typeConverts.remove(type);
        return this;
    }
    
    /**
     * Unregister a JSON {@link GenericConverter} with given property name
     * 
     * @param converter
     * @return
     */
    public <R, W> JSONer unregister(String property) {
        converts.remove(property);
        return this;
    }
    
    /**
     * Returns a new {@link WriteJSON} with delegate object
     * 
     * @return
     */
    public WriteJSON writer() {
        return new WriteJSON(this.delegate.orNull(), this);
    }
    
    /**
     * Returns a new {@link WriteJSON} with given object
     * 
     * @see JSONer#update(Object)
     * @see JSONer#writer()
     * @return
     */
    public WriteJSON writer(Object target) {
        return update(target).writer();
    }
    
    /**
     * @see JSONer#writer()
     * @see WriteJSON#asJson()
     */
    public String asJson() {
        return writer().asJson();
    }
    
    /**
     * @see JSONer#update(Object)
     * @see JSONer#asJson()
     */
    public String asJson(Object target) {
        return update(target).asJson();
    }
    
    /**
     * @see JSONer#writer()
     * @see WriteJSON#readable()
     * @see WriteJSON#asJson()
     */
    public String asFmtJson() {
        return writer().readable().asJson();
    }
    
    /**
     * @see JSONer#update(Object)
     * @see JSONer#asFmtJson()
     */
    public String asFmtJson(Object target) {
        return update(target).asFmtJson();
    }
    
    /**
     * Returns a new {@link ReadJSON} with delegate
     * 
     * @return
     */
    public ReadJSON reader() {
        Object obj = checkNotNull(this.delegate.orNull(), "The delegate object cannot be null");
        checkArgument(obj instanceof String, "The delegate object must be JSON string");
        return new ReadJSON((String) obj, this);
    }
    
    /**
     * @see JSONer#reader()
     * @see ReadJSON#map()
     */
    public Map<String, Object> asMap() {
        return reader().map();
    }
    
    /**
     * @see JSONer#reader()
     * @see ReadJSON#noneNullMap()
     */
    public Map<String, Object> asNoneNullMap() {
        return reader().noneNullMap();
    }
    
    /**
     * @see JSONer#reader()
     * @see ReadJSON#deepTierMap()
     */
    public Map<String, Object> asDeepTierMap() {
        return reader().deepTierMap();
    }
    
    /**
     * @see JSONer#reader()
     * @see ReadJSON#asObject(Object)
     */
    public <T> T asObject(Object target) {
        return reader().asObject(target);
    }
    
    /**
     * @see JSONer#reader()
     * @see ReadJSON#list()
     */
    public List<Object> asList() {
        return reader().list();
    }

    /**
     * Resets the delegate with given target
     * 
     * @param target
     * @return
     */
    public JSONer update(Object target) {
        delegate = Optional.fromNullable(target);
        return this;
    }
    
    /**
     * 
     */
    public static final class ReadJSON {
        
        /**
         * 
         */
        protected static final Log log = Loggers.from("JSONer.ReadJSON");
        
        /**
         * Returns the delegate JSON string as a given target instance
         * 
         * @param target
         * @return
         */
        public <T> T asObject(Object target) {
            return buildReflecter(Reflecter.from(target)).populate(noneNullMap()).get();
        }
        
        /**
         * Mapping property with given value mapping function
         * 
         * @param property
         * @param mappingFunc
         * @return
         */
        public <I, O> ReadJSON mapping(String property, Function<I, O> mappingFunc) {
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
        private ReadJSON(String target, JSONer jsoner) {
            this.delegate = new AsyncPushbackReader(
                    new BufferedReader(
                    new InputStreamReader(
                    new ByteArrayInputStream(target.getBytes(Charsets.UTF_8)), Charsets.UTF_8)));
            this.jsoner = Optional.fromNullable(jsoner);
        }
        
        static final char objL = '{';
        static final char objR = '}';
        static final char arrayL = '[';
        static final char arrayR = ']';
        static final char quotes = '"';
        static final char colon = ':';
        static final char slash = '\\';
        static final String itemsF = "$items";
        
        private <T> Reflecter<T> buildReflecter(final Reflecter<T> ref) {
            addJsonExchangeFunc(ref).autoExchange();
            
            if (jsoner.isPresent() && !jsoner.get().typeConverts.isEmpty()) {
                ref.fieldLoop(new Decisional<Field>() {

                    @Override protected void decision(Field input) {
                        Class<?> type = input.getType();
                        Converter<?, ?> converter = jsoner.get().getTypeConverter(type);
                        if (null != converter) {
                            ref.exchange(converter, input.getName());
                        }
                    }
                });
            }
            
            if (!this.mappingFuncs.isEmpty()) {
                ref.setExchangeFuncs(this.mappingFuncs);
            }
            
            return ref;
        }
        
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

        protected static final int MAX_CODE_POINT = 0x10ffff;
        protected static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;
        protected static final char MIN_LOW_SURROGATE = '\uDC00';
        protected static final char MIN_HIGH_SURROGATE = '\uD800';

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
        
        private <R, W> Object doConvert(boolean isPresent, String property, R readValue) {
            if (!isPresent) {
                return readValue;
            }
            
            Converter<R, W> converter = jsoner.get().getConverter(property);
            return null != converter ? converter.convert((R) readValue) : readValue;
        }
        
        private Map<String, Object> mappingBuild() throws IOException {
            boolean done = false, objectR = false;
            //'S' read start object, 'F' field, 'V' value, 'P' post
            char state = 'S';
            String field = null;
            Map<String, Object> map = Maps.newHashMap();
            final AsyncPushbackReader in = this.delegate;
            
            boolean isConvertPresent = jsoner.isPresent() && !jsoner.get().converts.isEmpty();
            
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
                    
                    map.put(field, doConvert(isConvertPresent, field, readValue()));
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
        private Optional<JSONer> jsoner = Optional.absent();
        private ReadJSON() {}
        
    }
    
    private Optional<?> delegate = null;
    private JSONer() {}
    private JSONer(Object target) {
        update(target);
    }
    
    private Map<String, Converter<?, ?>> converts = Maps.newHashMap();
    private Map<Class<?>, Converter<?, ?>> typeConverts = Maps.newHashMap();
    
    /**
     * Returns the {@link Converter} instance with given calss type key
     * 
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked") public <R, W> Converter<R, W> getTypeConverter(Class<?> key) {
        return (Converter<R, W>) typeConverts.get(key);
    }
    
    /**
     * Returns the {@link Converter} instance with given property name key
     * 
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked") public <R, W> Converter<R, W> getConverter(String key) {
        return (Converter<R, W>) converts.get(key);
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
    
}
