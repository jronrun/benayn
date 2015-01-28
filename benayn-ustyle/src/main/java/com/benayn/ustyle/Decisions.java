package com.benayn.ustyle;

import static com.google.common.base.Predicates.assignableFrom;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.isNull;
import static com.google.common.base.Predicates.or;
import static com.google.common.primitives.Primitives.isWrapperType;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

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
						if (IS_STRING.apply(input)) {
							return ((String) input).length() == 0;
						}
						
						if (IS_COLLECTION.apply(input)) {
							return ((Collection<?>) input).isEmpty();
						}
						
						if (IS_MAP.apply(input)) {
							return ((Map<?, ?>) input).isEmpty();
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
						
						return CharMatcher.WHITESPACE.matchesAllOf((String) input);
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
				return instanceOf(Object[].class).apply(input);
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
