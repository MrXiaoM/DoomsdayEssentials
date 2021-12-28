package top.mrxiaom.doomsdayessentials.external.haproxy;


import io.netty.handler.codec.DecoderException;

/**
 * A {@link DecoderException} which is thrown when an invalid HAProxy proxy protocol header is encountered
 * https://github.com/thijsa/SpigotProxy/blob/master/src/main/java/nl/thijsalders/spigotproxy/haproxy/HAProxyProtocolException.java
 * @author thijsa
 */
public class HAProxyProtocolException extends DecoderException {

    private static final long serialVersionUID = 713710864325167351L;

    /**
     * Creates a new instance
     */
    public HAProxyProtocolException() { }

    /**
     * Creates a new instance
     */
    public HAProxyProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance
     */
    public HAProxyProtocolException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     */
    public HAProxyProtocolException(Throwable cause) {
        super(cause);
    }
}