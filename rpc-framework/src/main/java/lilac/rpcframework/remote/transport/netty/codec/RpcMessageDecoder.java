package lilac.rpcframework.remote.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lilac.rpcframework.compress.Compress;
import lilac.rpcframework.enums.CompressType;
import lilac.rpcframework.enums.SerializationType;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.remote.dto.RpcMessage;
import lilac.rpcframework.remote.dto.RpcRequest;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static lilac.rpcframework.remote.constant.RpcConstant.*;

/**
 *  *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *  *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *  *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *  *   |                                                                                                       |
 *  *   |                                         body                                                          |
 *  *   |                                                                                                       |
 *  *   |                                        ... ...                                                        |
 *  *   +-------------------------------------------------------------------------------------------------------+
 *  * 8B  magic code（魔法数）   2B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 *  * 1B codec（序列化类型）      1B compress（压缩类型）   4B  requestId（请求的Id）
 *  * body（object类型数据）
 */

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // lengthFieldOffset: bytes length before "full_length" field = 8 + 2 = 10
        // lengthFieldLength: bytes length of "full_length" = 4
        // lengthAdjustment: the length of the beginning of frame to the end of "full_length" field = 8 + 2 + 4 = 14
        this(Integer.MAX_VALUE, 10, 4, -14, 0);
    }

    private RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip){
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 父类的decode方法只做切包，上面构造方法给的参数就是避免粘包的
        Object decodedBuf = super.decode(ctx, in);

        if (decodedBuf == null) {
            log.error("decoded buf is null");
            return null;
        }

        if (decodedBuf instanceof ByteBuf frame) {
            if (frame.readableBytes() >= HEAD_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("decode error: {}", e.getMessage());
                    return decodedBuf;
                } finally {
                    frame.release();
                }
            }
        }
        return decodedBuf;
    }


    /**
     * 解析数据帧的内容
     * @param in
     * @return
     */
    private Object decodeFrame(ByteBuf in) {
        if (!checkMagicNum(in)) return null;
        if (!checkVersion(in)) return null;

        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecCode = in.readByte();
        byte compressCode = in.readByte();
        int requestId = in.readInt();

        RpcMessage rpcMessage = RpcMessage.builder()
                .requestId(requestId)
                .messageType(messageType)
                .codec(codecCode)
                .compress(compressCode)
                .build();

        if (messageType == HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(PING);
        }
        if (messageType == HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(PONG);
        }

        int bodyLength = fullLength - HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] body = new byte[bodyLength];
            in.readBytes(body);

            String compressType = CompressType.getNameByCode(compressCode);
            Compress compress = Objects.requireNonNull(
                    ExtensionLoader.getExtensionLoader(Compress.class)).getExtension(compressType);
            body = compress.decompress(body);

            String codecType = SerializationType.getTypeByCode(codecCode);
            Serializer serializer = Objects.requireNonNull(
                    ExtensionLoader.getExtensionLoader(Serializer.class)).getExtension(codecType);
            if (messageType == REQUEST_TYPE) {
                RpcRequest rpcRequest = serializer.deserialize(body, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            } else if (messageType == RESPONSE_TYPE) {
                RpcResponse rpcResponse = serializer.deserialize(body, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            } else {
                log.warn("unknown message type {}", messageType);
                return null;
            }
        }
        return rpcMessage;

    }

    private boolean checkMagicNum(ByteBuf in) {
        int length = MAGIC_NUM.length;
        byte[] magicNum = new byte[length];
        in.readBytes(magicNum);
        for (int i = 0; i < magicNum.length; i++) {
            if (magicNum[i] != MAGIC_NUM[i]) {
                log.error("magic num is invalid: {}", new String(magicNum, StandardCharsets.UTF_8));
                return false;
            }
        }
        return true;
    }

    private boolean checkVersion(ByteBuf in) {
        int length = VERSION.length;
        byte[] version = new byte[length];
        in.readBytes(version);
        for (int i = 0; i < version.length; i++) {
            if (version[i] != VERSION[i]) {
                log.error("version is invalid: {}", new String(version, StandardCharsets.UTF_8));
                return false;
            }
        }
        return true;
    }

}
