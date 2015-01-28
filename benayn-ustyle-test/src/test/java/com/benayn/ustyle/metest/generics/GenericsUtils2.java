package com.benayn.ustyle.metest.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;

/**
 */
public class GenericsUtils2 {
	private static final Log log = Loggers.from(GenericsUtils2.class);

	private GenericsUtils2() {
	}

	/**
	 * 通过反射,获得定义Class时声明的父类的范型参数的类型. 如public BookManager extends
	 * GenricManager<Book>
	 * 
	 * @param clazz
	 *            The class to introspect
	 * @return the first generic declaration, or <code>Object.class</code> if
	 *         cannot be determined
	 */
	public static Class<?> getSuperClassGenricType(Class<?> clazz) {
		return getSuperClassGenricType(clazz, 0);
	}

	/**
	 * 通过反射,获得定义Class时声明的父类的范型参数的类型. 如public BookManager extends
	 * GenricManager<Book>
	 * 
	 * @param clazz
	 *            clazz The class to introspect
	 * @param index
	 *            the Index of the generic declaration,start from 0.
	 * @return the index generic declaration, or <code>Object.class</code> if
	 *         cannot be determined
	 */
	public static Class<?> getSuperClassGenricType(Class<?> clazz, int index) {

		Type genType = clazz.getGenericSuperclass();
		/* 判断是否继承自此类 */
		if (!(genType instanceof ParameterizedType)) {
			log.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		/* 判断传的参数：index 是否合法 */
		if (index >= params.length || index < 0) {
			log.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
					+ params.length);
			return Object.class;
		}
		/* 判断是否是真正的类型，因为可能是 泛化类型 T */
		if (!(params[index] instanceof Class<?>)) {
			log.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
			return Object.class;
		}
		return (Class<?>) params[index];
	}
}