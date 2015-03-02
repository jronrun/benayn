package com.benayn.pre;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.base.Domain;

public class ShowGenerics {
	public static void main(String[] args) {
		String pName = "complex"; // prop
		Field f = Reflecter.from(Domain.class).field(pName);
		String typeStr = ShowGenerics.typeToString(f.getGenericType());

		System.out.println(typeStr);
	}

	public static String typeToString(Type type) {
		StringBuilder sb = new StringBuilder();
		typeToString(sb, type, new HashSet<Type>());
		return sb.toString();
	}

	private static void typeToString(StringBuilder strB, Type type, Set<Type> visited) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
			strB.append(rawType.getName());

			boolean isPair = Map.class.isAssignableFrom(rawType);
			boolean first = true;
			int count = 0;
			Type[] types = parameterizedType.getActualTypeArguments();
			for (Type typeArg : types) {
				if (first) {
					first = false;
				} else {
					strB.append(", ");
				}
				if (!isPair || count == 0) {
					strB.append('<');
				}

				typeToString(strB, typeArg, visited);
				if (!isPair || count == (types.length - 1)) {
					strB.append('>');
				}
				++count;
			}
		} else if (type instanceof WildcardType) {
			WildcardType wildcardType = (WildcardType) type;
			strB.append("?");

			/*
			 * According to
			 * JLS(http://java.sun.com/docs/books/jls/third_edition/
			 * html/typesValues.html#4.5.1): - Lower and upper can't coexist:
			 * (for instance, this is not allowed: <? extends List<String> &
			 * super MyInterface>) - Multiple bounds are not supported (for
			 * instance, this is not allowed: <? extends List<String> &
			 * MyInterface>)
			 */
			final Type bound;
			if (wildcardType.getLowerBounds().length != 0) {
				strB.append(" super ");
				bound = wildcardType.getLowerBounds()[0];
			} else {
				strB.append(" extends ");
				bound = wildcardType.getUpperBounds()[0];
			}
			typeToString(strB, bound, visited);
		} else if (type instanceof TypeVariable<?>) {
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
					} else {
						strB.append(" & ");
					}
					typeToString(strB, bound, visited);
				}
				visited.remove(type);
			}
		} else if (type instanceof GenericArrayType) {
			GenericArrayType genericArrayType = (GenericArrayType) type;
			typeToString(genericArrayType.getGenericComponentType());
			strB.append(genericArrayType.getGenericComponentType());
			strB.append("[]");
		} else if (type instanceof Class) {
			Class<?> typeClass = (Class<?>) type;
			strB.append(typeClass.getName());
		} else {
			throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}
}