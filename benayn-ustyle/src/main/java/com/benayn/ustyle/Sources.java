package com.benayn.ustyle;

import static com.benayn.ustyle.Objects2.checkNotEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;

/**
 * https://github.com/jronrun/benayn
 */
public final class Sources {
	
	protected static final Log log = Loggers.from(Sources.class);
	
	private Sources() {}
	
	/**
	 * Returns the given source as a {@link Properties}
	 * 
	 * @param contextClass
	 * @param resourceName
	 * @return
	 * @throws IOException 
	 */
	public static Properties asProperties(Class<?> contextClass, String resourceName) throws IOException {
		Closer closer = Closer.create();
		try {
			Properties p = new Properties();
			p.load(closer.register(asCharSource(contextClass, resourceName).openStream()));
			return p;
		} catch (Throwable e) {
			throw closer.rethrow(e);
		} finally {
			closer.close();
		}
	}

	/**
	 * Returns the given source as a {@link String}.
	 * 
	 * @param contextClass
	 * @param resourceName
	 * @return
	 * @throws IOException 
	 */
	public static String asString(Class<?> contextClass, String resourceName) throws IOException {
		Closer closer = Closer.create();
		try {
			return CharStreams.toString(closer.register(asCharSource(contextClass, resourceName).openStream()));
		} catch (Throwable e) {
			throw closer.rethrow(e);
		} finally {
			closer.close();
		}
	}
	
	/**
     * @see Resources#getResource(Class, String)
     * @see Resources#toString(URL, Charset)
     */
    public static String asString(ClassLoader currentLoader, String resourceName) {
        URL url = checkNotNull(currentLoader).getResource(checkNotEmpty(resourceName));
        checkArgument(url != null, "resource %s relative to class loader %s not found.", resourceName, currentLoader.getClass());
        return toString(url, Charsets.UTF_8);
    }
    
    /**
     * @see Resources#getResource(String)
     * @see Resources#toString(URL, Charset)
     */
    public static String asString(String resourceName) {
        URL url = Resources.getResource(checkNotEmpty(resourceName));
        return toString(url, Charsets.UTF_8);
    }
    
    /**
     * @see com.google.common.io.LineProcessor
     * @see com.google.common.io.Resources#getResource(String)
     * @see com.google.common.io.Resources#readLines(URL, Charset, LineProcessor)
     */
    public static <T> T getResourceWith(String resourceName, LineProcessor<T> lineProcessor) {
        checkNotNull(lineProcessor);
        URL url = Resources.getResource(checkNotEmpty(resourceName));
        try {
            return Resources.readLines(url, Charsets.UTF_8, lineProcessor);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @see Resources#getDeepResource(ClassLoader, String)
     */
    public static URL getDeepResource(String resourceName) {
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        URL url = getDeepResource(currentLoader, resourceName);
        checkArgument(url != null, "resource %s not found for the current thread context", resourceName);
        return url;
    }

    /**
     * @param contextClass class to use as the context for class loader
     * @param resourceName  the resource name on class path
     * @return A {@link URL} object for reading the resource
     * @throws java.lang.IllegalArgumentException if the resource could not be found
     *      or the invoker doesn't have adequate privileges to get the resource
     * @see Resources#getDeepResource(ClassLoader, String)
     */
    public static URL getDeepResource(Class<?> contextClass, String resourceName) {
        ClassLoader currentLoader = checkNotNull(contextClass).getClassLoader();
        URL url = getDeepResource(currentLoader, resourceName);
        checkArgument(url != null,
                "resource %s not found for the context class %s", resourceName, contextClass.getCanonicalName());
        return url;
    }

    /**
     * Search class loader tree for resource, current implementation
     * attempts up to 10 levels of nested class loaders.
     *
     * @param startLoader class loader to start from
     * @param resourceName  the resource name on class path
     * @return A {@link URL} object for reading the resource
     * @throws java.lang.IllegalArgumentException if the resource could not be found
     *      or the invoker doesn't have adequate privileges to get the resource
     * @see java.lang.ClassLoader#getResource(String)
     */
    public static URL getDeepResource(ClassLoader startLoader, String resourceName) {
        ClassLoader currentLoader = checkNotNull(startLoader);
        URL url = currentLoader.getResource(checkNotEmpty(resourceName));
        int attempts = 0;
        while (url == null && attempts < 10) {  // search the class loader tree
            currentLoader = currentLoader.getParent();
            attempts += 1;
            //no inspection ConstantConditions
            if (currentLoader == null) {
                break;  // we are at the bootstrap class loader
            }
            url = currentLoader.getResource(resourceName);
        }
        checkArgument(url != null, "resource %s not found for the context class loader %s", resourceName, startLoader);
        return url;
    }
	
	/**
	 * Returns the given source as an {@link URL}
	 * 
	 * @param contextClass
	 * @param resourceName
	 * @return
	 */
	public static URL asURL(Class<?> contextClass, String resourceName) {
		return Resources.getResource(contextClass, resourceName);
	}
	
	/**
	 * Returns the given source as a {@link File}
	 * 
	 * @param contextClass
	 * @param resourceName
	 * @return
	 */
	public static File asFile(Class<?> contextClass, String resourceName) {
		return new File(asURL(contextClass, resourceName).getFile());
	}
	
	/**
	 * Returns the given source as a {@link CharSource}
	 * 
	 * @param contextClass
	 * @param resourceName
	 * @return
	 */
	public static CharSource asCharSource(final Class<?> contextClass, final String resourceName) {
		return asCharSource(contextClass, resourceName, Charsets.UTF_8);
	}
	
	/**
	 * Returns the given source as a {@link CharSource} with given charset
	 * 
	 * @param contextClass
	 * @param resourceName
	 * @param charset
	 * @return
	 */
	public static CharSource asCharSource(final Class<?> contextClass, final String resourceName, final Charset charset) {
		return new CharSource() {
			@Override public Reader openStream() throws IOException {
				return asReader(checkNotNull(contextClass).getResourceAsStream(checkNotEmpty(resourceName)), checkNotNull(charset));
			}
		};
	}
	
	/**
	 * Convert {@link InputStream} to {@link BufferedReader}
	 * 
	 * @param is
	 * @return
	 */
	public static BufferedReader asReader(InputStream is, Charset charset) {
		try {
			return new BufferedReader(new InputStreamReader(is, charset.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
     * @see Resources#toString(URL, Charset)
     */
    public static String toString(URL url, Charset charset) {
        try {
            return Resources.toString(checkNotNull(url), checkNotNull(charset));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
	
}
