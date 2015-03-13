/**
 * 
 */
package com.benayn.ustyle.thirdparty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;


/**
 * @see https://github.com/toonetown/guava-ext
 */
public class Uris {
    
    /** Our default protocol */
    private static final NetProtocol DEFAULT_PROTOCOL = NetProtocol.HTTP;
    
    /** An indicator of the scheme in a string */
    private static final String SCHEME_INDICATOR = "://";
    
    /**
     * Returns the scheme of the given URI or the scheme of the URI's port if there is a port. If there is no default,
     * then the scheme of the default protocol is returned.
     *
     * @param uri the URI to extract the scheme from
     * @return the scheme of the URI or the default protocol (http)
     * @throws NormalizationException if we tried to get the scheme based off of port, and we couldn't get the port
     */
    public static String getScheme(final URI uri) throws NormalizationException {
        final String scheme = Strings.emptyToNull(uri.getScheme());
        if (scheme == null && hasPort(uri)) {
            return NetProtocol.find(getPort(uri), DEFAULT_PROTOCOL).getScheme();
        } else if (scheme == null) {
            return DEFAULT_PROTOCOL.getScheme();
        }
        return scheme;
    }
    
    /**
     * Returns whether or not the given URI has userInfo
     *
     * @param uri the URI to check
     * @return true if the userInfo exists
     */
    public static boolean hasUserInfo(final URI uri) {
        return (getUserInfo(uri) != null);
    }
    
    /**
     * Returns the userInfo of the given URI, or null if it is empty.
     *
     * @param uri the URI to extract userInfo from
     * @return the user information or null if it is undefined
     */
    public static String getUserInfo(final URI uri) {
        return Strings.emptyToNull(uri.getUserInfo());
    }
    
    /**
     * Returns the raw userInfo of the given URI, or null if it is empty.
     *
     * @param uri the URI to extract userInfo from
     * @return the user information or null if it is undefined
     */
    public static String getRawUserInfo(final URI uri) {
        return Strings.emptyToNull(uri.getRawUserInfo());
    }
    
    /**
     * Returns the host of the given URI or throws an exception if the URI has no host.
     *
     * @param uri the URI to extract the host from
     * @return the extracted host
     * @throws NormalizationException if there is no host for the given uri
     */
    public static String getHost(final URI uri) throws NormalizationException {
        final String host = Strings.emptyToNull(uri.getHost());
        if (host == null) {
            throw new NormalizationException(uri.toString(), "No host in URI");
        }
        return host;
    }
    
    /**
     * Returns whether or not the given URI has a port explicitly specified.
     *
     * @param uri the URI to check
     * @return true if the port is explicitly specified
     */
    public static boolean hasPort(final URI uri) {
        return (uri.getPort() >= 0);
    }
    
    /**
     * Returns the port of the given URI.  If not specified, it returns the default port based on scheme.  If the
     * default port cannot be determined, then it throws an exception.
     *
     * @param uri the URI to extract the port from
     * @return the extracted port
     * @throws NormalizationException if there is no port, and we cannot determine a default.
     */
    public static int getPort(final URI uri) throws NormalizationException {
        if (hasPort(uri)) {
            return uri.getPort();
        }
        
        Integer port = NetProtocol.find(getScheme(uri)).getPort();
        if (null == port) {
        	throw new NormalizationException(uri.toString(), "Cannot determine port");
        }
        
        return port;
    }
    
    /** Prepends a string with a slash, if there isn't one already */
    private static String prependSlash(final String path) {
        if (path.length() == 0 || path.charAt(0) != '/') {
            /* our path doesn't start with a slash, so we prepend it */
            return "/" + path;
        }
        return path;
    }
    
    /**
     * Returns the path of the given URI - prefixed with a "/".  This means that an empty path becomes a single
     * slash.
     *
     * @param uri the URI to extract the path from
     * @return the extracted path
     */
    public static String getPath(final URI uri) {
        return prependSlash(Strings.nullToEmpty(uri.getPath()));
    }
    
    /**
     * Returns the raw (and normalized) path of the given URI - prefixed with a "/".  This means that an empty path
     * becomes a single slash.
     *
     * @param uri the URI to extract the path from
     * @param strict whether or not to do strict escaping
     * @return the extracted path
     */
    public static String getRawPath(final URI uri, final boolean strict) {
        return esc(strict).escapePath(prependSlash(Strings.nullToEmpty(uri.getRawPath())));
    }
    
    /** Returns the directory portion of the given path */
    private static String getDirectory(final String path) {
        return path.substring(0, path.lastIndexOf('/') + 1);
    }

    /**
     * Returns the directory portion of the given URI.  The directory is the path, up to and including the last
     * slash.  This means that all directories start and end with a slash (but may be a single slash only).
     *
     * @param uri the URI to extract the directory from
     * @return the extracted directory
     */
    public static String getDirectory(final URI uri) {
        return getDirectory(getPath(uri));
    }

    /**
     * Returns the raw (and normalized) directory portion of the given URI.  The directory is the raw path, up to and 
     * including the last slash.  This means that all directories start and end with a slash (but may be a single slash 
     * only).
     *
     * @param uri the URI to extract the directory from
     * @param strict whether or not to do strict escaping
     * @return the extracted directory
     */
    public static String getRawDirectory(final URI uri, final boolean strict) {
        return esc(strict).escapePath(getDirectory(getRawPath(uri, strict)));
    }

    /** Returns the file portion of the given path */
    private static String getFile(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    /**
     * Returns the file portion of the given URI.  The file is everything in the path after (but not including) the 
     * last slash.  This could also be an empty string.
     *
     * @param uri the URI to extract the file from
     * @return the extracted file
     */
    public static String getFile(final URI uri) {
        return getFile(getPath(uri));
    }
    
    /**
     * Returns the raw (and normalized) file portion of the given URI.  The file is everything in the raw path after 
     * (but not including) the last slash.  This could also be an empty string, but will not be null.
     *
     * @param uri the URI to extract the file from
     * @param strict whether or not to do strict escaping
     * @return the extracted file
     */
    public static String getRawFile(final URI uri, final boolean strict) {
        return esc(strict).escapePath(getFile(getRawPath(uri, strict)));
    }
    
    /**
     * Returns whether or not the given URI has a query
     *
     * @param uri the URI to check
     * @return true if the query exists
     */
    public static boolean hasQuery(final URI uri) {
        return (getQuery(uri) != null);
    }
    
    /**
     * Returns the query of the given URI, or null if it is empty.
     *
     * @param uri the URI to extract query from
     * @return the query or null if it is undefined
     */
    public static String getQuery(final URI uri) {
        return Strings.emptyToNull(uri.getQuery());
    }

    /**
     * Returns the raw (and normalized) query of the given URI, or null if it is empty.
     *
     * @param uri the URI to extract query from
     * @param strict whether or not to do strict escaping
     * @return the query or null if it is undefined
     */
    public static String getRawQuery(final URI uri, final boolean strict) {
        return esc(strict).escapeQuery(Strings.emptyToNull(uri.getRawQuery()));
    }

    /**
     * Returns whether or not the given URI has a fragment
     *
     * @param uri the URI to check
     * @return true if the fragment exists
     */
    public static boolean hasFragment(final URI uri) {
        return (getFragment(uri) != null);
    }
    
    /**
     * Returns the fragment of the given URI, or null if it is empty.
     *
     * @param uri the URI to extract fragment from
     * @return the fragment or null if it is undefined
     */
    public static String getFragment(final URI uri) {
        return Strings.emptyToNull(uri.getFragment());
    }
    
    /**
     * Returns the raw (and normalized) fragment of the given URI, or null if it is empty.
     *
     * @param uri the URI to extract fragment from
     * @param strict whether or not to do strict escaping
     * @return the fragment or null if it is undefined
     */
    public static String getRawFragment(final URI uri, final boolean strict) {
        return esc(strict).escapeFragment(Strings.emptyToNull(uri.getRawFragment()));
    }
    
    /**
     * Returns the entire URL as a string with its raw components normalized
     *
     * @param uri the URI to convert
     * @param strict whether or not to do strict escaping
     * @return the raw string representation of the URI
     * @throws NormalizationException if the given URI doesn't meet our stricter restrictions
     */
    public static String toRawString(final URI uri, final boolean strict) throws NormalizationException {
        final StringBuffer sb = new StringBuffer(getScheme(uri)).append("://");
        if (hasUserInfo(uri)) {
            sb.append(getRawUserInfo(uri)).append('@');
        }
        sb.append(getHost(uri)).append(':').append(getPort(uri)).append(getRawPath(uri, strict));
        if (hasQuery(uri)) {
            sb.append('?').append(getRawQuery(uri, strict));
        }
        if (hasFragment(uri)) {
            sb.append('#').append(getRawFragment(uri, strict));
        }
        return sb.toString();
    }
    
    /**
     * Returns a new URI that is the given path resolved against the given URI.
     * <ul><li>If the path begins with a slash ("/"), it is resolved against the domain as an absolute path.</li>
     *     <li>If the path begins with a dot and a slash ("./"), it is resolved against the directory of the UIR,
     *         retaining any existing file (or directory)</li>
     *     <li>If the path begins with a question mark ("?"), it is resolved against the file of the URL, replacing
     *         any existing query.</li>
     *     <li>If the path begins with an ampersand ("&"), it adds to any query that exists, creating it if needed.</li>
     *     <li>If the path begins with a hash ("#"), it replaces any fragment that exists.</li>
     *     <li>Any other path is resolved against the directory of the URI, replacing any file that exists</li></ul>
     *
     * The resulting URI is normalized.
     *
     * @param uri the base URI to resolve against
     * @param path the path to resolve
     * @param strict whether or not to perform strict escaping. (defaults to false)
     * @param strictNorm whether or not to perform strict normalization. (defaults to strict)
     * @return the resolved, normalized URI
     * @throws NormalizationException if there was a problem normalizing the URL
     */
    public static URI resolve(final URI uri,
                              final String path,
                              final boolean strict,
                              final boolean strictNorm) throws NormalizationException {
        final String query = '?' + Strings.nullToEmpty(getRawQuery(uri, strict));
        
        /* Append our ampersand stuff */
        final String myPath = (path.indexOf('&') == 0) ?
                                    (query + (hasQuery(uri) ? path : path.substring(1))) :
                                    path;
        final String ePath = esc(strict).escape(myPath);
        if (myPath.indexOf('/') == 0) {
            /* We can just resolve it directly */
            return normalize(uri.resolve(URI.create(ePath)), strictNorm);
        } else if (myPath.indexOf("./") == 0) {
            return normalize(uri.resolve(URI.create(getRawPath(uri, strict) + ePath.substring(1))), strictNorm);
        } else if (myPath.indexOf('?') == 0) {
            /* Resolve it against the raw path */
            return normalize(uri.resolve(URI.create(getRawPath(uri, strict) + ePath)), strictNorm);
        } else if (myPath.indexOf('#') == 0) {
            return normalize(uri.resolve(URI.create(getRawPath(uri, strict) + query + ePath)), strictNorm);
        }
        /* Resolve it against the raw directory */
        return normalize(uri.resolve(URI.create(getRawDirectory(uri, strict) + ePath)), strictNorm);
    }
    public static URI resolve(final URI uri, final String path, final boolean strict) throws NormalizationException {
        return resolve(uri, path, strict, strict);
    }
    public static URI resolve(final URI uri, final String path) throws NormalizationException {
        return resolve(uri, path, false);
    }

    /* Joiners to use for resolving parameters */
    private static final Joiner ENTRY_JOINER = Joiner.on("=");
    private static final Joiner PARAM_JOINER = Joiner.on("&");

    /* Returns a set of parameters for the given map - escaping the values as needed */
    private static Iterable<String> getParametersFromMap(final Map<String, String> paramMap, final boolean strict) {
        return Iterables.transform(paramMap.entrySet(), new Function<Map.Entry<String, String>, String>() {
            @Override public String apply(final Map.Entry<String, String> input) {
                return input == null ? null : ENTRY_JOINER.join(UriEscaper.escapeQueryParam(input.getKey(), strict),
                                                                UriEscaper.escapeQueryParam(input.getValue(), strict));
            }
        });
    }

    /**
     * Returns a new URI that is the given URI resolved with the given query parameters resolved (appended).  Both
     * the keys and values of each entry in the map will be normalized as queryParameter
     *
     * @param uri the base URI to resolve against
     * @param params the query parameters to resolve
     * @param strict whether or not to perform strict escaping. (defaults to false)
     * @param strictNorm whether or not to perform strict normalization. (defaults to strict)
     * @return the resolved, normalized URI
     * @throws NormalizationException if there was a problem normalizing the URL
     */
    public static URI resolveParams(final URI uri,
                                    final Map<String, String> params,
                                    final boolean strict,
                                    final boolean strictNorm) throws NormalizationException {
        return resolve(uri, "&" + PARAM_JOINER.join(getParametersFromMap(params, strict)), strictNorm);
    }
    public static URI resolveParams(final URI uri,
                                    final Map<String, String> params,
                                    final boolean strict) throws NormalizationException {
        return resolveParams(uri, params, strict, strict);
    }
    public static URI resolveParams(final URI uri, final Map<String, String> params) throws NormalizationException {
        return resolveParams(uri, params, false);
    }

    /**
     * Returns a new URI with the host portion replaced by the new host portion
     *
     * @param uri the uri to replace the host in
     * @param newHost the new host to replace with
     * @param strict whether or not to perform strict escaping. (defaults to false)
     * @return the new URI with everything the same except for the host being replaced
     * @throws URISyntaxException if the newly-created URI is malformed.
     */
    public static URI replaceHost(final URI uri, final String newHost, final boolean strict) throws URISyntaxException {
        final URI hostUri = newUri(newHost, strict);
        return newUri(uri.toString().replaceFirst(Pattern.quote(uri.getHost()),
                                                  Matcher.quoteReplacement(hostUri.getHost())), strict);
    }
    public static URI replaceHost(final URI uri, final String newHost) throws URISyntaxException {
        return replaceHost(uri, newHost, false);
    }

    /**
     * Returns a URI that has been truncated to its domain.
     *
     * @param uri the URI to resolve to the domain level
     * @param strict whether or not to do strict escaping
     * @return the resolved and normalized URI
     * @throws NormalizationException if there was a problem normalizing the URL
     */
    public static URI toDomain(final URI uri, final boolean strict) throws NormalizationException {
        return resolve(uri, "/", strict);
    }

    /**
     * Returns a URI that has been truncated to its directory.
     *
     * @param uri the URI to resolve to the directory level
     * @param strict whether or not to do strict escaping
     * @return the resolved and normalized  URI
     * @throws NormalizationException if there was a problem normalizing the URL
     */
    public static URI toDirectory(final URI uri, final boolean strict) throws NormalizationException {
        return resolve(uri, getRawDirectory(uri, strict), strict);
    }
    
    /**
     * Returns a URI that has been truncated to its path.
     *
     * @param uri the URI to resolve to the path level
     * @param strict whether or not to do strict escaping
     * @return the resolved and normalized  URI
     * @throws NormalizationException if there was a problem normalizing the URL
     */
    public static URI toPath(final URI uri, final boolean strict) throws NormalizationException {
        return resolve(uri, getRawPath(uri, strict), strict);
    }
    
    /**
     * Returns whether or not the given URI is normalized according to our rules.
     *
     * @param uri the uri to check normalization status
     * @param strict whether or not to do strict escaping
     * @return true if the given uri is already normalized
     */
    public static boolean isNormalized(final URI uri, final boolean strict) {
        return !Strings.isNullOrEmpty(uri.getScheme()) &&
                Objects.equal(Uris.getRawUserInfo(uri), uri.getRawUserInfo()) &&
                !Strings.isNullOrEmpty(uri.getHost()) &&
                hasPort(uri) &&
                Objects.equal(Uris.getRawPath(uri, strict), uri.getRawPath()) &&
                Objects.equal(Uris.getRawQuery(uri, strict), uri.getRawQuery()) &&
                Objects.equal(Uris.getRawFragment(uri, strict), uri.getRawFragment());
    }

    /**
     * Normalizes a java.net.URI using our own getScheme, getHost, getPort, and getPath.  The returned URI will
     * be constructed such that each of its functions will return the same - i.e. 
     * outUri.getScheme().equals(Uris.getScheme(inUri)).
     *
     * @param uri the URI to normalize
     * @param strict whether or not to do strict escaping
     * @return the normalized URI, or the same URI if the input is already normalized.
     * @throws NormalizationException if the given URI doesn't meet our stricter restrictions
     */
    public static URI normalize(final URI uri, final boolean strict) throws NormalizationException {
        if (isNormalized(uri, strict)) {
            return uri;
        }
        return URI.create(toRawString(uri, strict));
    }
    
    /**
     * Returns a normalized java.net.URI based off the given string url.  You should use this function instead
     * of "new URI(String)" - because this one handles escaping and normalization correctly.
     *
     * @param url the string to parse
     * @param strict whether or not to perform strict escaping. (defaults to false)
     * @return the parsed, normalized URI
     * @throws NormalizationException if the given string is invalid, or doesn't meet our stricter restrictions
     * @throws URISyntaxException if a java.net.URI could not be created from the string.
     */
    public static URI newUri(final String url, final boolean strict) throws URISyntaxException {
        /* 
         * Java's parsing thinks that the host is the scheme if there isn't a scheme.  Add the default if there is
         * no scheme yet
         */
        checkNotNull(Strings.emptyToNull(url), "Cannot create URI for null or empty value");
        if (url.indexOf(SCHEME_INDICATOR) < 0) {
            return normalize(new URI(DEFAULT_PROTOCOL.getScheme() + SCHEME_INDICATOR + esc(strict).escape(url)),
                             strict);
        } else {
            return normalize(new URI(esc(strict).escape(url)), strict);
        }
    }
    public static URI newUri(final String url) throws URISyntaxException { return newUri(url, false); }

    /**
     * Creates a new URI based off the given string.  This function differs from newUri in that it throws an
     * AssertionError instead of a URISyntaxException - so it is suitable for use in static locations as long as
     * you can be sure it is a valid string that is being parsed.
     *
     * @param url the string to parse
     * @param strict whether or not to perform strict escaping. (defaults to false)
     * @return the parsed, normalized URI
     */
    public static URI createUri(final String url, final boolean strict) {
        try {
            return newUri(url, strict);
        } catch (URISyntaxException e) {
            throw new AssertionError("Error creating URI: " + e.getMessage());
        }
    }

    /** Private function to return the escaper to use based off the strict flag */
    private static UriEscaper esc(final boolean strict) {
        return (strict ? UriEscaper.strictInstance() : UriEscaper.instance());
    }
    
    private Uris() {}

    /**
     * A subclass of URISyntaxException which indicates a normalization exception
     */
    public static class NormalizationException extends URISyntaxException {
    	
        /**
		 * 
		 */
		private static final long serialVersionUID = 1308079122934301553L;

		private NormalizationException(final String str, final String message) { super(str, message); }
    }    
}
