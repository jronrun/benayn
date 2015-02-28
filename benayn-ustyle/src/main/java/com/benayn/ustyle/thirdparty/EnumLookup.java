/**
 * 
 */
package com.benayn.ustyle.thirdparty;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;


/**
 * @see https://github.com/toonetown/guava-ext
 */
public class EnumLookup<K extends Enum<K> & EnumLookup.Findable, V> extends ForwardingMap<K, V> {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(EnumLookup.class);
	
	/**
     * Creates an EnumLookup instance for the enumerate of the given class.  
     * If this is a Keyed<String> enumerate, the finding will happen case-insensitively
     */
    public static <K extends Enum<K> & Keyed<V>, V> EnumLookup<K, V> of(final Class<K> enumClass) {
        return new EnumLookup<K, V>(enumClass, -1, false);
    }

    /**
     * Creates an EnumLookup instance for the enumerate of the given class
     */
    public static <K extends Enum<K> & Keyed<String>> EnumLookup<K, String> of(final Class<K> enumClass,
                                                                               final boolean caseSensitive) {
        return new EnumLookup<K, String>(enumClass, -1, caseSensitive);
    }

    /**
     * Creates an EnumLookup instance for the multi-keyed enumerate of the given class.  
     * If this is a MultiKeyed<String> enumerate, the finding will happen case-insensitively
     */
    public static <K extends Enum<K> & MultiKeyed, V> EnumLookup<K, V> of(final Class<K> enumClass, final int idx) {
        return new EnumLookup<K, V>(enumClass, idx, false);
    }

    /**
     * Creates an EnumLookup instance for the multi-keyed enumerate of the given class
     */
    public static <K extends Enum<K> & MultiKeyed> EnumLookup<K, String> of(
    		final Class<K> enumClass, final int idx, final boolean caseSensitive) {
        return new EnumLookup<K, String>(enumClass, idx, caseSensitive);
    }
    
	/**
	 * 
	 */
    interface Findable { }
    
    /**
     * An enum can implement this interface and be keyed via an EnumLookup
     */
    public interface Keyed<V> extends Findable {
    	
        /** 
         * Returns the value for this enumerate
         */
        V getValue();
    }
    
    /**
     * An enumerate can implement this interface multiple times and be keyed via an EnumLookup
     */
    public interface MultiKeyed extends Findable {
    	
        /**
         * Returns the array of key values for this enumerate
         */
        Object[] getValue();
    }
    
    /**
     * Finds the given ID and returns the corresponding enumerate value
     */
    public K find(final V id) {
        final V keyValue = keyForValue(id);
        
        if (inverse.containsKey(keyValue)) {
            return inverse.get(keyValue);
        }
        
        if (log.isDebugEnabled()) {
        	log.debug("Enum with value " + id + " not found");
        }
        
        return null;
    }
    
    /**
     * Finds the given ID and returns the corresponding enumerate or defaultValue if null
     */
    public K find(final V id, final K defaultValue) {
    	return firstNonNull(find(id), defaultValue);
    }
    
    /**
     * 
     */
    private EnumLookup(final Class<K> enumClass, final int idx, final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        final BiMap<K, V> biMap = EnumHashBiMap.create(checkNotNull(enumClass));
        
        for (final K k : enumClass.getEnumConstants()) {
            biMap.put(k, keyForValue(extractKeyValue(k, idx)));
        }
        
        this.delegate = Maps.unmodifiableBiMap(biMap);
        this.inverse = delegate.inverse();
    }

    @Override protected BiMap<K, V> delegate() { 
    	return delegate; 
    }
    
    //case-sensitivity
    private final boolean caseSensitive;
    private final BiMap<V, K> inverse;
    private final BiMap<K, V> delegate;
    
    @SuppressWarnings("unchecked") private V keyForValue(final V value) {
        if (value instanceof String && !caseSensitive) {
            return (V) ((String) value).toLowerCase();
        }
        
        return value;
    }
    
    @SuppressWarnings("unchecked") private V extractKeyValue(final Findable findable, final int idx) {
        if (findable instanceof MultiKeyed) {
            return (V) ((MultiKeyed) findable).getValue()[idx];
        } else {
            return ((Keyed<V>) findable).getValue();
        }
    }
    
}