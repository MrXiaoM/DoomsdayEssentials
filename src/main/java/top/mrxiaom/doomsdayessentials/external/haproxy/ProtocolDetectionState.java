package top.mrxiaom.doomsdayessentials.external.haproxy;

/**
 * https://github.com/thijsa/SpigotProxy/blob/master/src/main/java/nl/thijsalders/spigotproxy/haproxy/ProtocolDetectionState.java
 * @author thijsa
 */
public enum ProtocolDetectionState {
    /**
     * Need more data to detect the protocol.
     */
    NEEDS_MORE_DATA,

    /**
     * The data was invalid.
     */
    INVALID,

    /**
     * Protocol was detected,
     */
    DETECTED
}