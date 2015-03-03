/**
 * 
 */
package com.benayn.ustyle;

import static com.google.common.base.Throwables.propagate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

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
			    propagate(e);
			} catch (IllegalAccessException e) {
			    propagate(e);
			} catch (ClassNotFoundException e) {
			    propagate(e);
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
			    propagate(e);
			} catch (IllegalAccessException e) {
			    propagate(e);
			}
			
			return null;
		}
		
	}
	
	/**
     * Returns a supplier that always supplies {@link Constructor} with given parameter types
     */
	public static <T> Supplier<Constructor<T>> constructor(Class<T> clazz, Class<?>... parameterTypes) {
        return new ConstructorSupplier<T>(clazz, parameterTypes);
    }
	
	/**
     * Returns a supplier that always supplies {@link Constructor} with given parameters
     */
    public static <T> Supplier<Constructor<T>> constructor(Class<T> clazz, Object... parameters) {
        List<Class<?>> parameterTypes = Lists.newLinkedList();
        for (Object parameter : parameters) {
            parameterTypes.add(parameter.getClass());
        }
        
        return constructor(clazz, parameterTypes.toArray(new Class<?>[parameterTypes.size()]));
    }
	
	private static class ConstructorSupplier<T> implements Supplier<Constructor<T>> {
        
        Class<T> clazz;
        Class<?>[] parameterTypes;
        
        private ConstructorSupplier(Class<T> clazz, Class<?>... parameterTypes) {
            this.clazz = clazz;
            this.parameterTypes = parameterTypes;
        }

        @Override public Constructor<T> get() {
            try {
                if (null == this.clazz) {
                    return null;
                }
                
                if (null == this.parameterTypes || this.parameterTypes.length == 0) {
                    return (Constructor<T>) clazz.getDeclaredConstructor();
                }
                
                return (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
            } catch (NoSuchMethodException e) {
                propagate(e);
            } catch (SecurityException e) {
                propagate(e);
            }
            
            return null;
        }
        
    }
	
	/**
     * Returns a supplier that always supplies {@link Constructor} new instance with given parameter types
     */
    public static <T> Supplier<T> newInstance(Class<T> clazz, Object... parameters) {
        return new ClassConstructorNewInstanceSupplier<T>(clazz, parameters);
    }
	
	private static class ClassConstructorNewInstanceSupplier<T> implements Supplier<T> {
        
        Class<T> clazz;
        Object[] parameters;
        
        private ClassConstructorNewInstanceSupplier(Class<T> clazz, Object... parameters) {
            this.clazz = clazz;
            this.parameters = parameters;
        }

        @Override public T get() {
            return null == this.clazz 
                    ? null : newInstance(constructor(clazz, parameters).get(), parameters).get();
        }
        
    }
	
	/**
     * Returns a supplier that always supplies new instance with given parameter types
     */
    public static <T> Supplier<T> newInstance(Constructor<T> constructor, Object... parameters) {
        return new ConstructorNewInstanceSupplier<T>(constructor, parameters);
    }
	
	private static class ConstructorNewInstanceSupplier<T> implements Supplier<T> {

	    Constructor<T> constructor;
	    Object[] parameters;
	    
	    private ConstructorNewInstanceSupplier(Constructor<T> constructor, Object... parameters) {
            this.constructor = constructor;
            this.parameters = parameters;
        }
	    
        @Override public T get() {
            if (null == this.constructor) {
                return null;
            }
            
            constructor.setAccessible(true);
            
            try {
                return (T) ((null == parameters || parameters.length == 0) 
                        ? constructor.newInstance() : constructor.newInstance(parameters));
            } catch (InstantiationException e) {
                propagate(e);
            } catch (IllegalAccessException e) {
                propagate(e);
            } catch (IllegalArgumentException e) {
                propagate(e);
            } catch (InvocationTargetException e) {
                propagate(e);
            }
            
            return null;
        }
	    
	}
	
	/**
     * Returns a supplier that always supplies the given method invoke result
     */
    public static <R> Supplier<R> call(Object target, Method method, Object... parameters) {
        return new MethodSupplier<R>(target, method, parameters);
    }
	
	private static class MethodSupplier<R> implements Supplier<R> {
	    
		Object target; Method method; Object[] parameters;
        
        private MethodSupplier(Object target, Method method, Object... parameters) {
            this.target = target;
            this.method = method;
            this.parameters = parameters;
        }

        @SuppressWarnings("unchecked") @Override public R get() {
            if (null == target || null == method) {
                return null;
            }
            
            try {
                return (R) ((null != parameters && parameters.length > 0) 
                        ? method.invoke(target, parameters) : method.invoke(target));
            } catch (IllegalAccessException e) {
                propagate(e);
            } catch (IllegalArgumentException e) {
                propagate(e);
            } catch (InvocationTargetException e) {
                propagate(e);
            }
            
            return null;
        }
        
	}
}
