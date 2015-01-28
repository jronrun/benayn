/**
 * 
 */
package com.benayn.ustyle;

import com.google.common.base.Supplier;

/**
 *
 */
public final class Suppliers2 {
	
	private Suppliers2() {}
	
	/**
	 * Returns a supplier that always supplies new {@code instance} with given class full name
	 * 
	 * @param classFullName
	 * @return
	 */
	public static <T> Supplier<T> toInstance(String classFullName) {
		return new NameToInstanceSupplier<T>(classFullName);
	}
	
	private static class NameToInstanceSupplier<T> implements Supplier<T> {
		
		String classFullName;
		
		private NameToInstanceSupplier(String classFullName) {
			this.classFullName = classFullName;
		}

		@SuppressWarnings("unchecked") @Override public T get() {
			try {
				return (T) Class.forName(classFullName).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			return null;
		}
	}

	/**
	 * Returns a supplier that always supplies new {@code instance} with given class
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> Supplier<T> toInstance(Class<?> clazz) {
		return new ToInstanceSupplier<T>(clazz);
	}
  
	private static class ToInstanceSupplier<T> implements Supplier<T> {
		
		Class<?> clazz;
		
		private ToInstanceSupplier(Class<?> clazz) {
			this.clazz = clazz;
		}

		@SuppressWarnings("unchecked") @Override public T get() {
			try {
				return (T) clazz.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
	}
}
