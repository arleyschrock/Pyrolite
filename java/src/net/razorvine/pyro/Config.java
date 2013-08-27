package net.razorvine.pyro;

import java.io.Serializable;

/**
 * Minimalistic holders for Pyro config items.
 *
 * @author Irmen de Jong (irmen@razorvine.net)
 * @version 1.20
 */
public final class Config implements Serializable {
	private static final long serialVersionUID = 198635706890570066L;

	public static byte[] HMAC_KEY = null;
	public static String MSG_TRACE_DIR = null;
    public static int NS_PORT = 9090;
    public static int NS_BCPORT = 9091;

    public final static int PROTOCOL_VERSION = 46;    // Pyro 4.22 and newer. Cannot be changed

	public final static String PYROLITE_VERSION = "1.20";
}
