package com.benayn.ustyle;

import static com.google.common.base.Predicates.assignableFrom;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.isNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.primitives.Primitives.isWrapperType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.benayn.ustyle.string.Strs;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public final class Decisions {
	
	private Decisions() {}
	
	/**
	 * Returns a predicate that evaluates to {@code true} 
	 * if the object reference being tested is null or empty collection or empty map or empty string ("").
	 * 
	 * @return
	 */
	public static <T> Decision<T> isEmpty() {
		return ObjDecision.IS_EMPTY.withNarrowedType();
	}
	
	/**
	 * Checks if the target being tested is an instance of the given class
	 * 
	 * @param target
	 * @param clazz
	 * @return
	 */
	public static boolean isInstanceOf(Object target, Class<?> clazz) {
		return instanceOf(clazz).apply(target);
	}
	
	/**
	 * Checks if the target is being tested is assignable from the given class
	 * 
	 * @param target
	 * @param clazz
	 * @return
	 */
	public static boolean isAssignableFrom(Class<?> target, Class<?> clazz) {
		return assignableFrom(clazz).apply(target);
	}
	
	/**
	 * Checks if a String is whitespace, empty ("") or null
	 * 
	 * @return
	 */
	public static Decision<String> isBlank() {
		return ObjDecision.IS_BLANK.withNarrowedType();
	}
	
	/**
	 * Checks if the object reference being tested is instance of primitive types
	 * 
	 * @return
	 */
	public static <T> Decision<T> isPrimitive() {
		return ObjDecision.IS_PRIMITIVE.withNarrowedType();
	}
	
	/**
	 * Checks if the object reference being tested is instance of primitive types or String or Map or Collection
	 * 
	 * @return
	 */
	public static <T> Decision<T> isBaseStructure() {
		return ObjDecision.IS_BASE_STRUCTURE.withNarrowedType();
	}
	
	/**
	 * Checks if the object reference being tested is instance of Map
	 * 
	 * @return
	 */
	public static <T> Decision<T> isMap() {
		return ObjDecision.IS_MAP.withNarrowedType();
	}
	
	
	/**
	 * Checks if the object reference being tested is instance of object array
	 * 
	 * @return
	 */
	public static <T> Decision<T> isArray() {
		return ObjDecision.IS_ARRAY.withNarrowedType();
	}
	
	/**
	 * Checks if the object reference being tested is instance of Date
	 * 
	 * @return
	 */
	public static <T> Decision<T> isDate() {
		return ObjDecision.IS_DATE.withNarrowedType();
	}
	
	/**
	 * Checks if the object reference being tested is instance of Collection
	 * 
	 * @return
	 */
	public static <T> Decision<T> isCollection() {
		return ObjDecision.IS_COLLECTION.withNarrowedType();
	}
	
	/**
	 * Checks if the object reference being tested is instance of Class
	 * 
	 * @return
	 */
	public static <T> Decision<T> isClass() {
		return ObjDecision.IS_CLASS.withNarrowedType();
	}
	
	/**
	 * Checks if the object reference being tested is primitive types or String or Map or Collection
	 * 
	 * @return
	 */
	public static <T> Decision<T> isBaseClass() {
		return ClazzDecision.IS_BASE_CLASS.withNarrowedType();
	}
	
	/**
	 * Checks if the given class name is exists
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isClazzExists(String name) {
		try {
			Class.forName(name, false, Decisions.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	public static <T> Predicate<T> nor(Decision<? super T> first, Decision<? super T> second) {
        return not(or(first, second));
    }

    public static <T> Predicate<T> nor(@SuppressWarnings("unchecked") Decision<? super T>... components) {
        return not(or(components));
    }
    
    /**
     * Returns true if the given {@link Class} to provide a public constructor, else false
     */
    public static Decision<Class<?>> instantiatable() {
        return INSTANTIATABLE;
    }
    
    /**
     * Returns true if the given {@link Class} to not provide a public constructor, else false
     */
    public static Decision<Class<?>> notInstantiatable() {
        return NOT_INSTANTIATABLE;
    }
    
    private static final Decision<Class<?>> INSTANTIATABLE = new Decision<Class<?>>() {
        
        @Override public boolean apply(Class<?> input) {
            return !NOT_INSTANTIATABLE.apply(input);
        }
    };
    
    private static final Decision<Class<?>> NOT_INSTANTIATABLE = new Decision<Class<?>>() {

        @Override public boolean apply(Class<?> input) {
            try {
                if (null == input) {
                    return true;
                }
                
                final Constructor<?> constructor = input.getDeclaredConstructor();
                if (constructor.isAccessible() || !Modifier.isPrivate(constructor.getModifiers())) {
                    return false;
                }

                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (NoSuchMethodException e) {
                return false;
            } catch (InstantiationException e) {
                return true;
            } catch (InvocationTargetException e) {
                return true;
            } catch (IllegalAccessException e) {
                return true;
            }

            return true;
        }
    };
	
	/**
     * Returns a stateful decision that returns true if its argument has been passed to the decision before, else false.
     */
    public static <T> Decision<T> unique() {
        return new Decision<T>() {
            private Set<T> seen = Sets.newHashSet();
            private boolean sawNull = false;
            
            @Override public boolean apply(T arg) {
                if (arg == null) {
                    boolean result = !sawNull;
                    sawNull = true;
                    return result;
                } 
                else if (seen.contains(arg)) { 
                    return false;
                }
                else {
                    seen.add(arg);
                    return true;
                }
            }
        };        
    }
	
	public enum ClazzDecision implements Decision<Class<?>> {
		IS_MAP_CLASS {
			@Override public boolean apply(Class<?> input) {
				return assignableFrom(Map.class).apply(input);
			}
		},
		
		IS_COLLECTION_CLASS {
			@Override public boolean apply(Class<?> input) {
				return assignableFrom(Collection.class).apply(input);
			}
		},
		
		IS_STRING_CLASS {
			@Override public boolean apply(Class<?> input) {
				return assignableFrom(String.class).apply(input);
			}
		},
		
		IS_PRIMITIVE_CLASS {
			@Override public boolean apply(Class<?> input) {
				return isWrapperType(input);
			}
		},
		
		IS_BASE_CLASS {
			@Override public boolean apply(Class<?> input) {
				return or(ImmutableList.of(IS_STRING_CLASS, IS_MAP_CLASS, IS_COLLECTION_CLASS, IS_PRIMITIVE_CLASS)).apply(input);
			}
		};
		
		@SuppressWarnings("unchecked") // these Object predicates work for any T
		<T> Decision<T> withNarrowedType() {
			return (Decision<T>) this;
		}
	}
	
	/**
	 * 
	 */
	public enum ObjDecision implements Decision<Object> {
		IS_EMPTY {
			@Override public boolean apply(Object input) {
				return or(isNull(), new Decision<Object>() {
					
					@Override public boolean apply(Object input) {
						//CharSequence
						if (input instanceof CharSequence) {
							return ((CharSequence) input).length() == 0;
						} 
						//Collection
						else if (IS_COLLECTION.apply(input)) {
							return ((Collection<?>) input).isEmpty();
						} 
						//Map
						else if (IS_MAP.apply(input)) {
							return ((Map<?, ?>) input).isEmpty();
						} 
						//Optional
						else if (input instanceof Optional) {
							return !((Optional<?>) input).isPresent();
						} 
						//Iterable
						else if (input instanceof Iterable) {
							return !((Iterable<?>) input).iterator().hasNext();
						} 
						//Iterator
						else if (input instanceof Iterator) {
							return !((Iterator<?>) input).hasNext();
						}
						//Array
						else if (IS_ARRAY.apply(input)) {
							return Arrays2.wraps(input).length == 0;
						}
						
						return false;
					}
				}).apply(input);
			}
		},
		
		IS_BLANK {
			@Override public boolean apply(Object input) {
				return or(IS_EMPTY, new Decision<Object>() {
					
					@Override public boolean apply(Object input) {
						if (!IS_STRING.apply(input)) {
							return false;
						}
						
						return Strs.WHITESPACE.matchesAllOf((String) input);
					}
				}).apply(input);
			}
		},
		
		IS_STRING {
			@Override public boolean apply(Object input) {
				return instanceOf(String.class).apply(input);
			}
		},
		
		IS_MAP {
			@Override public boolean apply(Object input) {
				return instanceOf(Map.class).apply(input);
			}
		},
		
		IS_DATE {
			@Override public boolean apply(Object input) {
				return instanceOf(Date.class).apply(input);
			}
		},
		
		IS_ARRAY {
			@Override public boolean apply(Object input) {
				//return instanceOf(Object[].class).apply(input);
				return null == input ? false : input.getClass().isArray();
			}
		},
		
		IS_CLASS {
			@Override public boolean apply(Object input) {
				return instanceOf(Class.class).apply(input);
			}
		},
		
		IS_COLLECTION {
			@Override public boolean apply(Object input) {
				return instanceOf(Collection.class).apply(input);
			}
		},
		
		IS_PRIMITIVE {
			@Override public boolean apply(Object input) {
				if (isNull().apply(input)) {
					return false;
				}
				
				return isWrapperType(input.getClass());
			}
		},
		
		IS_BASE_STRUCTURE {
			@Override public boolean apply(Object input) {
				return or(ImmutableList.of(IS_STRING, IS_MAP, IS_COLLECTION, IS_PRIMITIVE, IS_DATE)).apply(input);
			}
		};
		
		@SuppressWarnings("unchecked") // these Object predicates work for any T
		<T> Decision<T> withNarrowedType() {
			return (Decision<T>) this;
		}
	}
	
}
