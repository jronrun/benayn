package com.benayn.ustyle;

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
import com.google.common.io.Resources;

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
				return asReader(contextClass.getResourceAsStream(resourceName), charset);
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
	
}
