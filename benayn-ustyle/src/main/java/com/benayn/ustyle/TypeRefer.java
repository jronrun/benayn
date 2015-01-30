package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

public final class TypeRefer {
	
	/**
	 * Returns a new {@link TypeRefer} instance with given {@link Type}
	 * 
	 * @param target
	 * @return
	 */
	public static TypeRefer of(Type target) {
		return new TypeRefer(target);
	}
	
	/**
	 * Returns a new {@link TypeRefer} instance with given {@link Field}
	 * 
	 * @param target
	 * @return
	 */
	public static TypeRefer of(Field target) {
		return of(target.getGenericType());
	}
	
	/**
	 * Returns a new {@link TypeRefer} instance with given {@link TypeToken}
	 * 
	 * @param target
	 * @return
	 */
	public static <T> TypeRefer of(TypeToken<T> target) {
		return of(target.getType());
	}
	
	/**
     * Returns a new {@link TypeRefer} instance with given {@link Class}
     * 
     * @param target
     * @return
     */
    public static <T> TypeRefer of(Class<T> target) {
        return of(TypeToken.of(target));
    }
	
	/**
	 * Returns the delegate type as {@link TypeDescrib}
	 * 
	 * @return
	 */
	public TypeDescrib asTypeDesc() {
		return initTypeDescrib().get();
	}
	
	/**
	 * Returns the delegate type as describe {@link String}
	 * 
	 * @return
	 */
	public String asDesc() {
		return asTypeDesc().describe;
	}

	/**
	 * 
	 */
	public class TypeDescrib {
		protected int level;				//current level
		protected boolean isPair;			//is key value type, example: Map<K, V>
		protected String describe;			//string describe
		protected Class<?> rawType;			//current raw type
		protected TypeDescrib superType;	//current type's super type
		
		protected List<TypeDescrib> childTypes;   //current type's child types
		
		private TypeDescrib(int level) { this.level = level; }
		
		protected void add(TypeDescrib type) {
		    if (null == this.childTypes) {
		        childTypes = Lists.newLinkedList();
		    }
		    
		    childTypes.add(type);
		}
		
		/**
		 * Returns the raw class
		 * 
		 * @return
		 */
		public Class<?> rawClazz() {
			return this.rawType;
		}
		
		/**
		 * Returns true if type has child type
		 * 
		 * @return
		 */
		public boolean hasChild() {
			return null != childTypes && childTypes.size() > 0;
		}
		
		/**
		 * Returns the child type length
		 * 
		 * @return
		 */
		public int size() {
		    if (!this.hasChild()) {
		        return 0;
		    }
		    
		    return this.childTypes.size();
		}
		
		/**
		 * Returns true if is pair type, like {@link Map}
		 * 
		 * @return
		 */
		public boolean isPair() {
			return this.isPair;
		}
		
		/**
		 * Returns the next level generic major type
		 * 
		 * @return
		 */
		public TypeDescrib next() {
			return next(0);
		}
		
		/**
         * Returns the next level generic major type with given serial number
         * 
         * @return
         */
        public TypeDescrib next(int serialNumber) {
            if (!this.hasChild() || serialNumber > this.childTypes.size()) {
                return null;
            }
            
            TypeDescrib describ = this.childTypes.get(serialNumber);
            describ.superType = this;
            return describ;
        }
		
		/**
		 * Returns the next level generic second type, only {@link Map}'s sub instance consider pair
		 * 
		 * @return
		 */
		public TypeDescrib nextPairType() {
			return this.isPair ? next(1) : null;
		}
		
		/**
		 * Returns the type full description
		 * 
		 * @return
		 */
		public String desc() {
			return this.describe;
		}

		@Override public String toString() {
			ToStringHelper helper = MoreObjects.toStringHelper(TypeDescrib.class);
			helper.add("level", this.level);
			helper.add("isPair", this.isPair);
			helper.add("describe", this.describe);
			
			if (null != this.rawType) {
				helper.add("rawType", this.rawType.getName());
			}
			
			if (this.hasChild()) {
			    int idx = 0;
			    for (TypeDescrib typeDescrib : this.childTypes) {
			        helper.add("childType" + idx, typeDescrib.toString());
			        idx++;
                }
			}
			
			return helper.toString();
		}
		
	}
	
	private TypeRefer(Type type) {
		this.delegate = checkNotNull(type);
	}
	
	private Optional<TypeDescrib> initTypeDescrib() {
		if (this.describ.isPresent()) {
			return this.describ;
		}
		
		return (this.describ = Optional.of(analystType(this.delegate)));
	}
	
	private void doWildcardTypeAalyst(StringBuilder strB, 
			WildcardType wildcardType, Set<Type> visited, TypeDescrib typeDescrib) {
		strB.append("?");

		/*
		 * According to JLS(http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.5.1):
		 * - Lower and upper can't coexist: (for instance, this is not allowed: <? extends List<String> & super MyInterface>) 
		 * - Multiple bounds are not supported (for instance, this is not allowed: <? extends List<String> & MyInterface>)
		 */
		final Type bound;
		if (wildcardType.getLowerBounds().length != 0) {
			strB.append(" super ");
			bound = wildcardType.getLowerBounds()[0];
		} else {
			strB.append(" extends ");
			bound = wildcardType.getUpperBounds()[0];
		}
		
		TypeDescrib children = newInnerRef(typeDescrib.level + 1);
		typeDescrib.add(children);
		
		StringBuilder strUnit = new StringBuilder();
		analystType(strUnit, bound, visited, children);
		children.describe = strUnit.toString();
		strB.append(strUnit);
	}
	
	private void doTypeVariableAnalyst(StringBuilder strB, Type type, Set<Type> visited, TypeDescrib typeDescrib) {
		TypeDescrib children = null;
		int nextLevel = typeDescrib.level + 1;
		
		TypeVariable<?> typeVariable = (TypeVariable<?>) type;
		strB.append(typeVariable.getName());
		/*
		 * Prevent cycles in case: <T extends List<T>>
		 */
		if (!visited.contains(type)) {
			visited.add(type);
			strB.append(" extends ");
			boolean first = true;
			for (Type bound : typeVariable.getBounds()) {
				if (first) {
					first = false;
					children = newInnerRef(nextLevel);
					typeDescrib.add(children);
				} else {
					strB.append(" & ");
					children = newInnerRef(nextLevel);
				}
				
				StringBuilder strUnit = new StringBuilder();
				analystType(strUnit, bound, visited, children);
				children.describe = strUnit.toString();
				strB.append(strUnit);
			}
			visited.remove(type);
		}
	}
	
	private void doGenericArrayTypeAnalyst(StringBuilder strB, 
			GenericArrayType genericArrayType, Set<Type> visited, TypeDescrib typeDescrib) {
		typeDescrib.add(analystType(genericArrayType.getGenericComponentType()));
		strB.append(genericArrayType.getGenericComponentType());
		strB.append("[]");
	}
	
	private void doClassAnalyst(StringBuilder strB, 
			Class<?> typeClass, Set<Type> visited, TypeDescrib typeDescrib) {
		typeDescrib.rawType = typeClass;
		strB.append(typeClass.getName());
	}

	private void analystType(StringBuilder strB, Type type, Set<Type> visited, TypeDescrib typeDescrib) {
		if (type instanceof ParameterizedType) {
			doParameterizedTypeAnalyst(strB, (ParameterizedType) type, visited, typeDescrib);
		} else if (type instanceof WildcardType) {
			doWildcardTypeAalyst(strB, (WildcardType) type, visited, typeDescrib);
		} else if (type instanceof TypeVariable<?>) {
			doTypeVariableAnalyst(strB, type, visited, typeDescrib);
		} else if (type instanceof GenericArrayType) {
			doGenericArrayTypeAnalyst(strB, (GenericArrayType) type, visited, typeDescrib);
		} else if (type instanceof Class) {
			doClassAnalyst(strB, (Class<?>) type, visited, typeDescrib);
		} else {
			throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}
	
	private TypeDescrib analystType(Type target) {
		StringBuilder strB = new StringBuilder();
		TypeDescrib typeDescrib = newInnerRef(1);
		analystType(strB, target, Sets.<Type>newHashSet(), typeDescrib);
		typeDescrib.describe = strB.toString();
		return typeDescrib;
	}
	
	private void doParameterizedTypeAnalyst(StringBuilder strB, 
			ParameterizedType parameterizedType, Set<Type> visited, TypeDescrib typeDescrib) {
		TypeDescrib children = null;
		int nextLevel = typeDescrib.level + 1;
		final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
		
		strB.append(rawType.getName());
		boolean isPair = Map.class.isAssignableFrom(rawType);
		typeDescrib.isPair = isPair;
		typeDescrib.rawType = rawType;
		
		int count = 0;
		boolean first = true;
		Type[] types = parameterizedType.getActualTypeArguments();
		
		for (Type typeArg : types) {
			if (first) {
				first = false;
				children = newInnerRef(nextLevel);
				typeDescrib.add(children);
			} else {
				strB.append(", ");
				children = newInnerRef(nextLevel);
				typeDescrib.add(children);
			}
			
			if (!isPair || count == 0) {
				strB.append('<');
			}

			StringBuilder strUnit = new StringBuilder();
			analystType(strUnit, typeArg, visited, children);
			children.describe = strUnit.toString();
			strB.append(strUnit);
			
			if (!isPair || count == (types.length - 1)) {
				strB.append('>');
			}
			
			++count;
		}
	}
	
	private TypeDescrib newInnerRef(int level) {
		return new TypeDescrib(level);
	}
	
	private Type delegate = null;
	private Optional<TypeDescrib> describ = Optional.absent();
}
