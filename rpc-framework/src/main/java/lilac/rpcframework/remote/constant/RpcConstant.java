package lilac.rpcframework.remote.constant;

import lombok.Getter;

@Getter
public class RpcConstant {

    private static final byte[] MAGIC_NUM = {(byte) 'l', (byte) 'i', (byte) 'l', (byte) 'a', (byte) 'c',
            (byte) 'r', (byte) 'p', (byte) 'c'};

    // Version information
    public static final byte VERSION = 1;
    public static final byte TOTAL_LENGTH = 16;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    // ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    // ping
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final byte HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8*1024*1024;
}
