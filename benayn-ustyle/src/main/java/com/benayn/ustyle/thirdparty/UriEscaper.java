/**
 * 
 */
package com.benayn.ustyle.thirdparty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.common.base.CharMatcher;
import com.google.common.escape.Escaper;


/**
 * @see https://github.com/toonetown/guava-ext
 */
public class UriEscaper extends Escaper {

    private static final String PROTOCOL_SEPARATOR = "://";

    /* CharMatchers that match the URL spec - according to RFC 1808 */
    private static final CharMatcher ALPHA = CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('A', 'Z'));
    private static final CharMatcher DIGIT = CharMatcher.inRange('0', '9');
    private static final CharMatcher SAFE = CharMatcher.anyOf("$-_.+");
    private static final CharMatcher EXTRA = CharMatcher.anyOf("!*'(),");
    private static final CharMatcher UCHAR = ALPHA.or(DIGIT).or(SAFE).or(EXTRA);
    private static final CharMatcher PCHAR = UCHAR.or(CharMatcher.anyOf(":@&="));
    private static final CharMatcher RESERVED = CharMatcher.anyOf(";/?:@&=");
    
    /* Doesn't take into account whether or not the percent escape is valid */
    private static final CharMatcher ESCAPED = CharMatcher.is('%');
    private static final CharMatcher HEX = DIGIT.or(CharMatcher.inRange('a', 'f')).or(CharMatcher.inRange('A', 'F'));
    
    /* Separator matchers */
    private static final CharMatcher HOST_SEPARATOR = CharMatcher.is('/');
    private static final CharMatcher QUERY_SEPARATOR = CharMatcher.is('?');
    private static final CharMatcher FRAG_SEPARATOR = CharMatcher.is('#');
    private static final CharMatcher SPACE = CharMatcher.is(' ');
    private static final CharMatcher PLUS = CharMatcher.is('+');
    private static final CharMatcher ILLEGAL_IN_HOST = ALPHA.or(DIGIT).or(CharMatcher.anyOf(".-:/@")).negate();
    private static final CharMatcher ILLEGAL_IN_PATH = PCHAR.or(CharMatcher.is('/')).negate();
    private static final CharMatcher ILLEGAL_IN_QUERY = UCHAR.or(RESERVED).negate();
    private static final CharMatcher ILLEGAL_IN_QUERY_PARAM = ILLEGAL_IN_QUERY.or(CharMatcher.anyOf("&="));
    private static final CharMatcher ILLEGAL_IN_FRAGMENT = UCHAR.or(RESERVED).negate();
    
    /* Additional (stricter) matching */
    private static final CharMatcher STRICT_SAFE = ALPHA.or(DIGIT).or(SAFE);
    private static final CharMatcher STRICT_ILLEGAL_IN_PATH = STRICT_SAFE.or(CharMatcher.is('/')).negate();
    private static final CharMatcher STRICT_ILLEGAL_IN_QUERY = STRICT_SAFE.or(CharMatcher.anyOf("&=")).negate();
    private static final CharMatcher STRICT_ILLEGAL_IN_QUERY_PARAM = STRICT_ILLEGAL_IN_QUERY
                                                                                    .or(CharMatcher.anyOf("&=$+"));
    private static final CharMatcher STRICT_ILLEGAL_IN_FRAGMENT = STRICT_SAFE.negate();
    
    /**
     * A private enum for tracking our state
     */
    private enum State { HOST, PATH, QUERY, QUERY_PARAM, FRAGMENT }
    
    /**
     * Whether or not to do even stricter checking
     */
    private final boolean strict;
    
    @Override public String escape(final String string) {
        /* Just calls our recursive escaper */
        return escape(string, State.HOST);
    }
    
    protected UriEscaper(final boolean strict) { this.strict = strict; }

    /**
     * Shortcut function to escape just the path portion
     *
     * @param string the string to escape
     * @return a path-escaped string
     */
    public String escapePath(final String string) {
        /* Just calls our recursive escaper */
        return escape(string, State.PATH);
    }

    /**
     * Shortcut function to escape just the query portion
     *
     * @param string the string to escape
     * @return a query-escaped string
     */
    public String escapeQuery(final String string) {
        /* Just calls our recursive escaper */
        return escape(string, State.QUERY);
    }

    /**
     * Shortcut function to escape a string as a query parameter
     *
     * @param string the string to escape
     * @return a query-parameter-escaped string
     */
    public String escapeQueryParam(final String string) {
        /* Just calls our recursive escaper */
        return escape(string, State.QUERY_PARAM);
    }

    /**
     * Shortcut function to escape just the fragment portion
     *
     * @param string the string to escape
     * @return a fragment-escaped string
     */
    public String escapeFragment(final String string) {
        /* Just calls our recursive escaper */
        return escape(string, State.FRAGMENT);
    }

    /**
     * Returns the escaped string beginning in the given state.  This function can be called recursively.
     */
    private String escape(final String string, final State inState) {
        if (string == null) { return null; }
        State state = inState;
        final int length = string.length();
        for (int index = 0; index < length; index++) {
            /* We don't do anything with the protocol separator */
            if (state == State.HOST && string.indexOf(PROTOCOL_SEPARATOR) == index) {
                index += PROTOCOL_SEPARATOR.length();
                continue;
            }
            final char c = string.charAt(index);
            if (HOST_SEPARATOR.matches(c) && state == State.HOST) {
                state = State.PATH;
            }
            if (FRAG_SEPARATOR.matches(c) && state != State.FRAGMENT && state != State.QUERY_PARAM) {
                /* Switch state - don't do anything with this character */
                state = State.FRAGMENT;
            } else if (QUERY_SEPARATOR.matches(c) && (state == State.PATH || state == State.HOST)) {
                /* Switch state - don't do anything with this character */
                state = State.QUERY;
            } else {
                final String encodeString = encodeCharAtIndex(string, index, state);
                if (encodeString != null) {
                    /* We are supposed to encode this character - recursively call for the remainder of the string */
                    return string.substring(0, index) + encodeString + escape(string.substring(index + 1), state);
                }
            }
        }
        return string;
    }
    
    /** 
     * Returns whether or not this character is illegal in the given state
     */
    private boolean isIllegal(final char c, final State state) {
        switch (state) {
            case FRAGMENT:
                return (strict ? STRICT_ILLEGAL_IN_FRAGMENT : ILLEGAL_IN_FRAGMENT).matches(c);
            case QUERY:
                return (strict ? STRICT_ILLEGAL_IN_QUERY : ILLEGAL_IN_QUERY).matches(c);
            case QUERY_PARAM:
                return (strict ? STRICT_ILLEGAL_IN_QUERY_PARAM : ILLEGAL_IN_QUERY_PARAM).matches(c);
            case PATH:
                return (strict ? STRICT_ILLEGAL_IN_PATH : ILLEGAL_IN_PATH).matches(c);
            case HOST:
                return ILLEGAL_IN_HOST.matches(c);
            default:
                throw new AssertionError(state);
        }
    }
    
    /**
     * Encodes the character at the given index, and returns the resulting string.  If the character does not need to
     * be encoded, this function returns null
     */
    private String encodeCharAtIndex(final String string, final int index, final State state) {
        final char c = string.charAt(index);

        /* Special-case: spaces are "%20" when not within query string, or when doing strict mode */
        if ((SPACE.matches(c) && ((state != State.QUERY && state != State.QUERY_PARAM) || strict)) ||
            (strict && PLUS.matches(c) && state == State.QUERY)) {
            return "%20";
        }

        /* Check if there are two hex digits after a percent */
        if (ESCAPED.matches(c) && string.length() > index + 2 &&
            HEX.matches(string.charAt(index + 1)) && HEX.matches(string.charAt(index + 2))) {
            return null;
        }
        
        /* We got this far - so if we are an illegal character, escape it */
        if (isIllegal(c, state)) {
            try {
                /* Special-case: asterisks are not encoded by URLEncoder.encode */
                if (c == '*') {
                    return "%2A";
                } else {
                    return URLEncoder.encode(Character.toString(c), "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 always exists, " + e.getMessage());
            }
        }
        
        /* Not illegal - so return null */
        return null;
    }
    
    /**
     * Returns an instance of the escaper
     *
     * @return an instance of a UriEscaper
     */
    public static UriEscaper instance() { return ESCAPER; }
    private static final UriEscaper ESCAPER = new UriEscaper(false);

    /**
     * Returns a strict instance of the escaper
     *
     * @return a strict instance of a UriEscaper
     */
    public static UriEscaper strictInstance() { return STRICT_ESCAPER; }
    private static final UriEscaper STRICT_ESCAPER = new UriEscaper(true);

    /**
     * Escapes a string as a URI
     *
     * @param url the path to escape
     * @param strict whether or not to do strict escaping
     * @return the escaped string
     */
    public static String escape(final String url, final boolean strict) {
        return (strict ? STRICT_ESCAPER : ESCAPER).escape(url);
    }

    /**
     * Escapes a string as a URI path
     *
     * @param path the path to escape
     * @param strict whether or not to do strict escaping
     * @return the escaped string
     */
    public static String escapePath(final String path, final boolean strict) {
        return (strict ? STRICT_ESCAPER : ESCAPER).escapePath(path);
    }

    /**
     * Escapes a string as a URI query
     *
     * @param query the path to escape
     * @param strict whether or not to do strict escaping
     * @return the escaped string
     */
    public static String escapeQuery(final String query, final boolean strict) {
        return (strict ? STRICT_ESCAPER : ESCAPER).escapeQuery(query);
    }

    /**
     * Escapes a string as a URI query parameter (same as a strict query, but also escaping & and =)
     * @param queryParam the parameter to escape
     * @param strict whether or not to do strict escaping
     * @return the escaped string
     */
    public static String escapeQueryParam(final String queryParam, final boolean strict) {
        return (strict ? STRICT_ESCAPER : ESCAPER).escapeQueryParam(queryParam);
    }

    /**
     * Escapes a string as a URI query
     *
     * @param fragment the path to escape
     * @param strict whether or not to do strict escaping
     * @return the escaped string
     */
    public static String escapeFragment(final String fragment, final boolean strict) {
        return (strict ? STRICT_ESCAPER : ESCAPER).escapeFragment(fragment);
    }
}
