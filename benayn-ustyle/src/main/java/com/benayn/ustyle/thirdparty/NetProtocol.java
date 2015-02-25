/**
 * 
 */
package com.benayn.ustyle.thirdparty;

import java.util.Set;


/**
 * @see https://github.com/toonetown/guava-ext
 */
public enum NetProtocol implements EnumLookup.MultiKeyed {
    AFP     ("afp", 548),
    DNS     ("dns", 53),
    FTP     ("ftp", 21),
    GOPHER  ("gopher", 70),
    HTTP    ("http", 80),
    HTTPS   ("https", 443),
    IMAP    ("imap", 143),
    IPP     ("ipp", 631),
    IRIS    ("iris", 702),
    LDAP    ("ldap", 389),
    NNTP    ("nntp", 119),
    POP     ("pop", 110),
    RTSP    ("rtsp", 554),
    SMB     ("smb", 445),
    SMTP    ("smtp", 25),
    SNMP    ("snmp", 162),
    SSH     ("ssh", 22),
    TELNET  ("telnet", 23);

    /** The scheme of this protocol */
    private final String scheme;

    /** The port for this protocol */
    private final Integer port;
    
    private NetProtocol(String scheme, Integer port) {
    	this.scheme = scheme;
    	this.port = port;
    }

	public String getScheme() {
		return scheme;
	}

	public Integer getPort() {
		return port;
	}

	@Override public Object[] getValue() {
		return new Object[] { scheme, port };
	}

	private static final EnumLookup<NetProtocol, String> $BY_SCHEME = EnumLookup.of(NetProtocol.class, 0);
	private static final EnumLookup<NetProtocol, Integer> $BY_PORT = EnumLookup.of(NetProtocol.class, 1);

	/** Finds a single NetProtocol by scheme */
	public static NetProtocol find(final String scheme) {
		return $BY_SCHEME.find(scheme);
	}

	public static NetProtocol find(final String scheme, final NetProtocol protocol) {
		return $BY_SCHEME.find(scheme, protocol);
	}

	/** Finds a single NetProtocol by port */
	public static NetProtocol find(final Integer port) {
		return $BY_PORT.find(port);
	}

	public static NetProtocol find(final Integer port, final NetProtocol protocol) {
		return $BY_PORT.find(port, protocol);
	}

	/** Returns all NetProtocols */
	public static Set<NetProtocol> all() {
		return $BY_SCHEME.keySet();
	}
}

