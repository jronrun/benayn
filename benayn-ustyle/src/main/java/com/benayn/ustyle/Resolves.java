package com.benayn.ustyle;

import static com.benayn.ustyle.TypeRefer.of;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import com.benayn.ustyle.TypeRefer.TypeDescrib;
import com.benayn.ustyle.behavior.StructBehavior;
import com.benayn.ustyle.behavior.ValueBehavior;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Enums;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public final class Resolves {
    
    /**
     * 
     */
    protected static final Log log = Loggers.from(Resolves.class);
    
    public static <T> T get(Field field, Object input) {
        return get(of(checkNotNull(field)).asTypeDesc(), input);
    }
    
    public static <T> T get(Class<?> clazz, Object input) {
        return get(of(checkNotNull(clazz)).asTypeDesc(), input);
    }
    
    public static <T> T get(Type type, Object input) {
        return get(of(checkNotNull(type)).asTypeDesc(), input);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(TypeDescrib typeDescrib, Object input) {
        return (T) new ResolveStructBehavior(checkNotNull(typeDescrib), input).doDetect();
    }
    
    private static class ResolveStructBehavior extends StructBehavior<Object> {
        
        private Object input;
        private TypeDescrib type;
        
        public ResolveStructBehavior(TypeDescrib type, Object input) {
            super(checkNotNull(type.rawClazz(), "Type cannot be null"));
            this.type = type;
            this.input = input;
        }

        @Override protected Object booleanIf() {
            return Funcs.TO_BOOLEAN.apply(input);
        }

        @Override protected Object byteIf() {
            return Funcs.TO_BYTE.apply(input);
        }

        @Override protected Object characterIf() {
            return Funcs.TO_CHARACTER.apply(input);
        }

        @Override protected Object doubleIf() {
            return Funcs.TO_DOUBLE.apply(input);
        }

        @Override protected Object floatIf() {
            return Funcs.TO_FLOAT.apply(input);
        }

        @Override protected Object integerIf() {
            return Funcs.TO_INTEGER.apply(input);
        }

        @Override protected Object longIf() {
            return Funcs.TO_LONG.apply(input);
        }

        @Override protected Object shortIf() {
            return Funcs.TO_SHORT.apply(input);
        }

        @Override protected Object nullIf() {
            return null;
        }

        @Override protected Object afterNullIf() {
            if (null == input) {
                return null;
            }
            
            return toBeContinued();
        }

        @Override protected Object noneMatched() {
            return new ResolveValueBehavior(type, input).doDetect();
        }
        
    }
    
    private static class ResolveValueBehavior extends ValueBehavior<Object> {

        private Object input;
        private TypeDescrib type;
        
        public ResolveValueBehavior(TypeDescrib type, Object input) {
            super(type.rawClazz());
            this.type = type;
            this.input = input;
        }

        @Override protected <T> Object classIf(Class<T> resolvedP) { return toBeContinued(); }
        @Override protected Object primitiveIf() { return null; }
        @Override protected Object eightWrapIf() { return null; }

        @Override protected Object dateIf(Date resolvedP) {
            if (null != resolvedP) { return resolvedP; }
            return Funcs.TO_DATE.apply(input);
        }

        @Override protected Object stringIf(String resolvedP) {
            if (null != resolvedP) { return resolvedP; }
            return Funcs.TO_STRING.apply(input);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override protected Object enumif(Enum<?> resolvedP) {
            if (null != resolvedP) { return resolvedP; }
            return Enums.stringConverter((Class<Enum>) delegate).convert(input.toString().toUpperCase());
        }

        @Override
        protected <T> Object arrayIf(T[] resolvedP, boolean isPrimitive) {
            if (null != resolvedP) { return resolvedP; }
            
            if (input.getClass().isArray()) {
                if (isPrimitive) {
                    return Arrays2.unwraps((Object[]) input);
                }
                
                return Arrays2.wraps(input, type.rawClazz());
            }
            
            return Arrays2.wraps(get(type.rawClazz().getComponentType(), input));
        }

        @Override protected Object bigDecimalIf(BigDecimal resolvedP) {
            if (null != resolvedP) { return resolvedP; }
            return Funcs.TO_BIGDECIMAL.apply(input);
        }

        @Override protected Object bigIntegerIf(BigInteger resolvedP) {
            if (null != resolvedP) { return resolvedP; }
            return Funcs.TO_BIGINTEGER.apply(input);
        }

        @Override
        protected <K, V> Object mapIf(Map<K, V> resolvedP) {
            return resolveValue(type, input);
        }

        @Override
        protected <T> Object setIf(Set<T> resolvedP) {
            return resolveValue(type, input);
        }

        @Override
        protected <T> Object listIf(List<T> resolvedP) {
            return resolveValue(type, input);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object beanIf() {
            try {
                //input JSON object string
                if (input instanceof String && isJSONObject((String) input)) {
                    return JSONer.read((String) input).asObject(type.rawClazz());
                } else if (Map.class.isAssignableFrom(input.getClass())) {
                    return Reflecter.from(this.clazz).populate((Map<String, ?>) input).get();
                }
                
                return this.clazz.cast(input);
            } catch (Exception e) {
                log.warn("bean if cast expect: " + this.clazz.getName() + 
                        ", actual: " + input.getClass().getName() + ", failed: " + e.getMessage());
            }
            
            return null;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" }) private Object resolveValue(TypeDescrib type, Object value) {
            Class<?> clazz = type.rawClazz();
            
            if (type.hasChild()) {
                boolean isInterf = clazz.isInterface();
                
                //Map
                if (Map.class.isAssignableFrom(clazz)) {
                    Map map = null;
                    if (isInterf) {
                        map = Maps.newHashMap();
                    } else {
                        map = (Map) Suppliers2.toInstance(clazz).get();
                    }
                    
                    // value Map
                    if (value instanceof Map) {
                        Map<?, ?> m = (Map<?, ?>) value;
                        for (Entry<?, ?> e : m.entrySet()) {
                            map.put(resolveValue(type.next(), e.getKey()), resolveValue(type.nextPairType(), e.getValue()));
                        }
                    }
                    
                    return map;
                }
                //Set
                else if (Set.class.isAssignableFrom(clazz)) {
                    Set set = null;
                    if (isInterf) {
                        set = Sets.newHashSet();
                    } else {
                        set = (Set) Suppliers2.toInstance(clazz).get();
                    }
                    
                    // value set
                    if (value instanceof Set) {
                        Set<?> s = (Set<?>) value;
                        for (Object obj : s) {
                            set.add(resolveValue(type.next(), obj));
                        }
                    } 
                    // value array
                    else if (value.getClass().isArray()) {
                        Object[] objA = null;
                        if (Objects2.isPrimitive(value.getClass().getComponentType())) {
                            objA = Arrays2.wraps(value);
                        } else {
                            objA = (Object[]) value;
                        }
                        
                        for (Object obj : objA) {
                            set.add(resolveValue(type.next(), obj));
                        }
                    }
                    //value JSON array string
                    else if (value instanceof String && isJSONArray((String) value)) {
                        List<?> list = JSONer.readList((String) value);
                        for (Object obj : list) {
                            set.add(resolveValue(type.next(), obj));
                        }
                    }
                    
                    return set;
                }
                //List
                else if (List.class.isAssignableFrom(clazz)) {
                    List list = null;
                    if (isInterf) { 
                        list = Lists.newArrayList();
                    } else {
                        list = (List) Suppliers2.toInstance(clazz).get();
                    }
                    
                    // value list
                    if (value instanceof List) {
                        List<?> l = (List<?>) value;
                        for (Object obj : l) {
                            list.add(resolveValue(type.next(), obj));
                        }
                    } 
                    // value array
                    else if (value.getClass().isArray()) {
                        Object[] objA = null;
                        if (Objects2.isPrimitive(value.getClass().getComponentType())) {
                            objA = Arrays2.wraps(value);
                        } else {
                            objA = (Object[]) value;
                        }
                        
                        for (Object obj : objA) {
                            list.add(resolveValue(type.next(), obj));
                        }
                    }
                    //value JSON array string
                    else if (value instanceof String && isJSONArray((String) value)) {
                        List<?> l2 = JSONer.readList((String) value);
                        for (Object obj : l2) {
                            list.add(resolveValue(type.next(), obj));
                        }
                    }
                    
                    return list;
                }
                
            }

            return get(type, value);
        }
        
        private static final Pattern JSON_ARRAY = Pattern.compile("^\\[.*?\\]$");
        private static final Pattern JSON_OBJECT = Pattern.compile("^\\{.*?\\}$");
        
        private boolean isJSONObject(String target) {
            return (null == target || target.isEmpty()) ? false : JSON_OBJECT.matcher(target).find();
        }
        
        private boolean isJSONArray(String target) {
            return (null == target || target.isEmpty()) ? false : JSON_ARRAY.matcher(target).find();
        }

        @Override protected Object nullIf() {
            return null;
        }

        @Override protected Object afterNullIf() {
            if (null == input) {
                return null;
            }
            
            return toBeContinued();
        }
        
    }

}
