package lilac.rpcframework.remote.transport.netty.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lilac.rpcframework.compress.Compress;
import lilac.rpcframework.constants.Constants;
import lilac.rpcframework.enums.CompressType;
import lilac.rpcframework.enums.SerializationType;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.remote.dto.RpcMessage;
import lilac.rpcframework.serialize.Serializer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
 *  * 1B codec（序列化类型） 1B compress（压缩类型）   4B  requestId（请求的Id）
 *  * body（object类型数据）
 */

public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger MESSAGE_COUNTER = new AtomicInteger(0);

    private static final String compressType = Constants.COMPRESS_TYPE;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(MAGIC_NUM);
        byteBuf.writeBytes(VERSION);
        // skip for store full_length later
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);

        byteBuf.writeByte(rpcMessage.getMessageType());
        byteBuf.writeByte(rpcMessage.getCodec());
        byteBuf.writeByte(CompressType.getCodeByName(compressType));
        byteBuf.writeInt(MESSAGE_COUNTER.incrementAndGet());
        byte[] requestBody = null;

        int fullLength = byteBuf.writerIndex();
        if (rpcMessage.getMessageType() != HEARTBEAT_REQUEST_TYPE
                && rpcMessage.getMessageType() != HEARTBEAT_RESPONSE_TYPE) {
            String codecType = SerializationType.getTypeByCode(rpcMessage.getCodec());
            Serializer serializer = Objects.requireNonNull(
                    ExtensionLoader.getExtensionLoader(Serializer.class)).getExtension(codecType);
            requestBody = serializer.serialize(rpcMessage.getData());
            Compress compress = Objects.requireNonNull(
                    ExtensionLoader.getExtensionLoader(Compress.class)).getExtension(compressType);
            requestBody = compress.compress(requestBody);
            fullLength += requestBody.length;
        }

        if (requestBody != null) {
            byteBuf.writeBytes(requestBody);
        }

        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(writerIndex - fullLength + MAGIC_NUM.length + VERSION.length);
        byteBuf.writeInt(fullLength);
        byteBuf.writerIndex(writerIndex);
    }





















}
