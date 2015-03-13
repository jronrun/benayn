package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.benayn.ustyle.logger.Loggers;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapConstraint;
import com.google.common.collect.MapConstraints;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

public final class Mapper<K, V> {
	
	/**
	 * 
	 */
	protected final static Loggers logger = Loggers.of(Mapper.class);
	
	private Optional<Map<K, V>> delegate;
	
	protected Mapper() {}
	
	protected Mapper(Map<K, V> map) {
		this.delegate = Optional.of(map);
	}
	
	/**
	 * Returns a new {@link Mapper} instance
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V> Mapper<K, V> from(Map<K, V> map) {
		return new Mapper<K, V>(map);
	}
	
	/**
	 * Returns a new look deeply {@link Map} instance
	 * 
	 * @see Mapper#deepLook()
	 * @param map
	 * @return
	 */
	public static <K, V> Map<K, V> deeply(Map<K, V> map) {
		return from(map).deepLook().map();
	}
	
	/**
	 * Returns a new tier key {@link Map} instance
	 * 
	 * @see Mapper#tierKey()
	 * @param map
	 * @return
	 */
	public static <K, V> Map<K, V> tiers(Map<K, V> map) {
		return from(map).tierKey().map();
	}
	
	/**
     * Returns a new tier key and look deepLook {@link Map} instance
     * 
     * @see Mapper#tierKey()
     * @param map
     * @return
     */
    public static <K, V> Map<K, V> deeplyTiers(Map<K, V> map) {
        return from(map).deepLook().tierKey().map();
    }
	
	/**
	 * A constraint on the keys and values that may be added to a Map or Multimap
	 * 
	 * @param <K>
	 * @param <V>
	 */
	public interface Restraint<K, V> extends MapConstraint<K, V> {
		
	}
	
	/**
	 * Add a specified constraint to this map
	 * 
	 * @param constraint
	 * @return
	 */
	public Mapper<K, V> constraint(Restraint<K, V> constraint) {
		return addConstraint(constraint);
	}
	
	/**
	 * Add a constraint that verifies that neither the key nor the value is
	 * null. If either is null, a {@link NullPointerException} is thrown.
   	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Mapper<K, V> keyAndValueNotNull() {
		return addConstraint((MapConstraint<K, V>) MapConstraints.notNull());
	}
	
	/**
	 * Add a constraint that verifies that the key is
	 * null. If is null, a {@link NullPointerException} is thrown.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Mapper<K, V> keyNotNull() {
		return addConstraint((MapConstraint<K, V>) KeyNotNullRestraint.INSTANCE);
	}
	
	/**
	 * Add a constraint that verifies that the value is
	 * null. If is null, a {@link NullPointerException} is thrown.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Mapper<K, V> valueNotNull() {
		return addConstraint((MapConstraint<K, V>) ValueNotNullRestraint.INSTANCE);
	}
	
	/**
	 * Add a unique key constraint to this map
	 * 
	 * @return
	 */
	public Mapper<K, V> uniqueKey() {
		return addConstraint(new Restraint<K, V>() {
			
			@Override
			public void checkKeyValue(K key, V value) {
				checkArgument(!delegate.get().containsKey(key), "The key: %s is already exist.", key);
			}
		});
	}
	
	/**
	 * Returns the values {@link Gather#loop(Decision)}
	 * 
	 * @param decision
	 * @return
	 */
	public Gather<V> valueLoop(Decision<V> decision) {
		return valueGather().loop(decision);
	}
	
	/**
	 * Returns the keys {@link Gather#loop(Decision)}
	 * 
	 * @param decision
	 * @return
	 */
	public Gather<K> keyLoop(Decision<K> decision) {
		return keyGather().loop(decision);
	}
	
	/**
	 * Returns the entries {@link Gather#loop(Decision)}
	 * 
	 * @param decision
	 * @return
	 */
	public Gather<Entry<K, V>> entryLoop(Decision<Entry<K, V>> decision) {
		return entryGather().loop(decision);
	}
	
	/**
	 * Returns delegate map instance as a entry Gather
	 * 
	 * @return
	 */
	public Gather<Entry<K, V>> entryGather() {
		return Gather.from(delegate.get().entrySet());
	}
	
	/**
	 * Returns delegate map instance keys as a key Gather
	 * 
	 * @return
	 */
	public Gather<K> keyGather() {
		return Gather.from(delegate.get().keySet());
	}
	
	/**
	 * Returns delegate map instance values as a value Gather
	 * 
	 * @return
	 */
	public Gather<V> valueGather() {
		return Gather.from(delegate.get().values());
	}
	
	/**
	 * Filter map with a special decision
	 * 
	 * @param decision
	 * @return
	 */
	public Mapper<K, V> filter(Decision<Entry<K, V>> decision) {
		delegate = Optional.of(Maps.filterEntries(delegate.get(), decision));
		return this;
	}

	/**
	 * Returns the delegate map that apply the previous operate
	 * 
	 * @return
	 */
	public Map<K, V> map() {
		return delegate.get();
	}
	
	/**
	 * Returns a immutableMap instance, Conflict with deepLook()
	 * 
	 * @return
	 */
	public Map<K, V> immutableMap() {
		return ImmutableMap.copyOf(delegate.get());
	}
	
	/**
	 * Returns the {@link DeepLookMap} instance (Lookup key deep into within map)
	 * 
	 * @return
	 */
	public Mapper<K, V> deepLook() {
	    if (!(delegate.get() instanceof DeepLookMap)) {
	        delegate = Optional.of((Map<K, V>) DeepLookMap.from(delegate.get()));
	    }
		return this;
	}
	
	/**
	 * Returns the {@link TierKeyMap} instance, allowed the key like "a.b[0].c.d[3]", 
	 * and if the key class type is not {@link String} will happen nothing.
	 * 
	 * @return
	 */
	public Mapper<K, V> tierKey() {
	    if (!(delegate.get() instanceof TierKeyMap)) {
	        delegate = Optional.of((Map<K, V>) TierKeyMap.from(delegate.get()));
	    }
		return this;
	}

	/**
	 * Returns the value to which the specified key is mapped, deep look
	 * 
	 * @param key
	 * @return
	 */
	public <T> T get(K key) {
		return mapVal2(delegate.get(), key);
	}
	
	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the specified key, deep look 
     * 
	 * @param key
	 * @return
	 */
    public boolean containsKey(K key) {
    	return contains(delegate.get(), key, 'k');
    }
    
    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value, deep look
     * 
     * @param value
     * @return
     */
    public boolean containsValue(V value) {
    	return contains(delegate.get(), value, 'v');
    }
	
	/**
	 * Log all element with INFO level
	 * 
	 * @return
	 */
	public Mapper<K, V> info() {
		logger.humanStyle().info(delegate.get());
		return this;
	}
	
	/**
	 * Add a constrained view of the delegate map, using the specified constraint
	 * 
	 * @param constraint
	 * @return
	 */
	private Mapper<K, V> addConstraint(MapConstraint<K, V> constraint) {
		this.delegate = Optional.of(MapConstraints.constrainedMap(delegate.get(), constraint));
		return this;
	}
	
	/**
	 * 
	 * @param <K>
	 * @param <V>
	 */
	static class TierKeyMap<K, V> extends ForwardingMap<K, V> {
		private final Map<K, V> delegate;
		
		static <K, V> TierKeyMap<K, V> from(Map<K, V> delegate) {
			return new TierKeyMap<K, V>(delegate);
		}
		
		private TierKeyMap(Map<K, V> delegate) {
			this.delegate = checkNotNull(delegate);
		}
		
		@Override public boolean containsKey(Object key) {
			return this.<Boolean>tierOp('C', key, null);
		}

		@Override protected Map<K, V> delegate() {
			return delegate;
		}

		@Override public V get(Object key) {
			return tierOp('G', key, null);
		}

		@Override public V put(K key, V value) {
			return tierOp('P', key, value);
		}

		@Override public V remove(Object object) {
			return tierOp('R', object, null);
		}
		
		@SuppressWarnings("unchecked")
		private <R> R tierOp(Character op, Object key, V val) {
			if (!isTierKey(key) && !isCollKey(key)) {
				switch (op) {
				case 'G': return asTierR(delegate().get(key));
				case 'P': return asTierR(delegate().put((K) key, val));
				case 'R': return asTierR(delegate().remove(key));
				case 'C': return asTierR(delegate().containsKey(key));
				default: return null;
				}
			}
			
			V v = null;
			R r = null;
			Map<Object, V> m = null;
			//0 is not collection or array, 1 List, 2 Set, 3 Array
			//key, coll type, index
			Triple<String, Integer, Integer> collKey = null;
			
			String k = null;
			boolean isC = 'C' == op;
			String[] keys = asTierKey(key);
			for (int i = 0, l = keys.length; i < l; i++) {
			    k = keys[i];
			    
			    //Supports List, Set, Array
			    if (null != (collKey = asCollKey(k))) {
			        Object collVal = null;
			        v = (i == 0) ? delegate().get(collKey.getL()) : m.get(collKey.getL());
			        
			        //middle
			        if (i != (l - 1)) {
			            if (null == v) {
                            if ('P' == op) {
                                v = (V) Lists.newArrayList();
                                m = Maps.<Object, V>newHashMap();
                                ((List<Map<Object, V>>) v).add(m);
                                ((Map<Object, V>) (i == 0 ? delegate() : m)).put(collKey.getL(), v);
                                continue;
                            }
                            
                            return (R) (isC ? false : null);
                        } else {
                            if (null == (collVal = getCollObj(v, collKey)) || !(collVal instanceof Map)) {
                                return (R) (isC ? false : null);
                            }
                            
                            m  = (Map<Object, V>) collVal;
                            continue;
                        }
			        } 
			        //last
			        else {
			            if (null == v) {
			                if ('P' == op) {
                                v = (V) Lists.newArrayList();
                                ((List<V>) v).add(val);
                                ((Map<Object, V>) (i == 0 ? delegate() : m)).put(collKey.getL(), v);
                                return (R) v;
                            }
			                
			                return (R) (isC ? false : null);
			            }
			            
			            collVal = getCollObj(v, collKey);
			            if ('G' == op) {
                            return (R) (null == collVal ? null : asTierR(collVal));
                        } else if (isC) {
                            return (R) (null == collVal ? Boolean.FALSE : Boolean.TRUE);
                        } else if ('P' == op) {
                            switch (collKey.getC()) {
                                //list
                                case 1: ((List<V>) v).add(collKey.getR(), val); break;
                                //set
                                case 2: ((Set<V>) v).add(val); break;
                                //array
                                case 3:
                                    int len = Arrays2.length(v);
                                    if (len > collKey.getR()) {
                                        if (Objects2.isPrimitiveArray(v.getClass())) {
                                            Array.set(v, collKey.getR(), val);
                                        } else {
                                            ((Object[]) v)[collKey.getR()] = val;
                                        }
                                    } else {
                                        v = (V) Arrays2.add(v, 
                                                collKey.getR(), val, v.getClass().getComponentType());
                                    }
                                    ((Map<Object, V>) (i == 0 ? delegate() : m)).put(collKey.getL(), v);
                                    break;
                            }
                            
                            return (R) val;
                        } else if ('R' == op) {
                            switch (collKey.getC()) {
                                //list
                                case 1: ((List<V>) v).remove(collVal); break;
                                //set
                                case 2: ((Set<V>) v).remove(collVal); break;
                                //array
                                case 3: 
                                    v = (V) Arrays2.remove(v, collKey.getR()); 
                                    ((Map<Object, V>) (i == 0 ? delegate() : m)).put(collKey.getL(), v);
                                    break;
                            }
                            
                            return (R) collVal;
                        }
			        }
			    }
			    
			    //Map
				v = (i == 0) ? delegate().get(k) : m.get(k);
				if (i != (l - 1)) {
					if ('P' == op) {
						if (null == v) {
							v = (V) Maps.newHashMap();
							((Map<Object, V>) (i == 0 ? delegate() : m)).put(k, v);
						} else if (!(v instanceof Map)) {
							return null;
						}
					} else if (null == v || !(v instanceof Map)) {
						return (R) (isC ? false : null);
					}
				} else {
					switch (op) {
					case 'G': r = (R) v; break;
					case 'P': r = (R) m.put(k, val); break;
					case 'R': r = (R) m.remove(k); break;
					case 'C': r = (R) Boolean.valueOf(m.containsKey(k)); break;
					}
					break;
				}
				
				m = (Map<Object, V>) v;
			}
			
			return (R) ((null != r) ? asTierR(r) : (isC ? false : null));
		}
		
		private Object getCollObj(V v, Triple<String,Integer, Integer> collKey) {
		    Object collVal = null;
		    Class<?> vType = v.getClass();
		    int idx = collKey.getR();
		    
            if (vType.isArray()) {
                if (Objects2.isPrimitiveArray(vType)) {
                    int len = Arrays2.length(v);
                    if (len > idx) {
                        collVal = Array.get(v, idx);
                    }
                } else {
                    Object[] arr = (Object[]) v;
                    if (arr.length > idx) {
                        collVal = arr[idx];
                    }
                }
                collKey.setC(3);
            } else if (List.class.isAssignableFrom(vType)) {
                List<?> l = (List<?>) v;
                if (l.size() > idx) {
                    collVal = l.get(idx);
                }
                collKey.setC(1);
            } else if (Set.class.isAssignableFrom(vType)) {
                Set<?> s = (Set<?>) v;
                if (s.size() > idx) {
                    collVal = s.toArray()[idx];
                }
                collKey.setC(2);
            }
            
            return collVal;
		}
		
		private boolean isCollKey(Object k) {
		    return (null != k) && (k instanceof String) && COLL_KEY_CHECK_PATTERN.matcher((String) k).find();
		}
		
		private Triple<String,Integer, Integer> asCollKey(Object k) {
		    if (null == k || !(k instanceof String)) {
		        return null;
		    }
		    
		    Matcher m = COLL_KEY_PATTERN.matcher((String) k);
		    if (m.matches()) {
		        return Triple.of(m.group("key"), 0, Ints.tryParse(m.group("idx")));
		    }
		    
		    return null;
		}
		
		private boolean isTierKey(Object k) {
			return (null != k) && (k instanceof String) && (((String) k).indexOf(TIER_SEP) != Strs.INDEX_NONE_EXISTS);
		}
		
		private String[] asTierKey(Object k) {
			return Lists.newLinkedList(Splitter.on(TIER_SEP).split((String) k)).toArray(new String[]{});
		}
		@SuppressWarnings("unchecked")
		private <R> R asTierR(Object v) {
			return (R) ((null != v && v instanceof Map) ? tiers((Map<K, V>) v) : v);
		}
	}
	
	private static final String TIER_SEP = ".";
	private static final Pattern COLL_KEY_PATTERN = Pattern.compile("(?<key>.*)\\[(?<idx>.*)\\]");
	private static final Pattern COLL_KEY_CHECK_PATTERN = Pattern.compile("\\[[0-9]+\\]");
	
	/**
	 * 
	 * @param <K>
	 * @param <V>
	 */
	static class DeepLookMap<K, V> extends ForwardingMap<K, V> {
		private final Map<K, V> delegate;

		static <K, V> DeepLookMap<K, V> from(Map<K, V> delegate) {
			return new DeepLookMap<K, V>(delegate);
		}
		
		private DeepLookMap(Map<K, V> delegate) {
			this.delegate = checkNotNull(delegate);
		}

		@Override public boolean containsKey(Object key) {
			return contains(delegate, key, 'k');
		}

		@Override public boolean containsValue(Object value) {
			return contains(delegate, value, 'v');
		}

		@Override protected Map<K, V> delegate() {
			return delegate;
		}

		@Override public V get(Object key) {
			return mapVal2(delegate, key);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <K, V> boolean contains(Map<K, V> m, Object kv, char k1v2) {
		if ('k' == k1v2 && m.containsKey(kv)) {
			return true;
		}
		
		if ('v' == k1v2 && m.containsValue(kv)) {
			return true;
		}
		
		for (V v : m.values()) {
			if (v instanceof Map) {
				if (contains((Map<K, V>) v, kv, k1v2)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private static <T, K, V> T mapVal2(Map<K, V> m, Object k) {
		if (m.containsKey(k)) {
			return (T) m.get(k);
		}
		
		T val = null;
		for (V v : m.values()) {
			if (v instanceof Map) {
				if (null != (val = mapVal2((Map<K, V>) v, k))) {
					return val;
				}
			}
		}
		
		return null;
	}
	
	private enum KeyNotNullRestraint implements Restraint<Object, Object> {
		INSTANCE;
		@Override public void checkKeyValue(Object key, Object value) {
			checkNotNull(key, "The key must be not null.");
		}
	}
	
	private enum ValueNotNullRestraint implements Restraint<Object, Object> {
		INSTANCE;
		@Override public void checkKeyValue(Object key, Object value) {
			checkNotNull(value, "The value must be not null.");
		}
	}
	
}
