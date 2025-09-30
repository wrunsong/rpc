package lilac.rpcframework.remote.transport.netty.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lilac.rpcframework.remote.dto.RpcMessage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *  *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *  *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *  *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *  *   |                                                                                                       |
 *  *   |                                         body                                                          |
 *  *   |                                                                                                       |
 *  *   |                                        ... ...                                                        |
 *  *   +-------------------------------------------------------------------------------------------------------+
 *  * 8B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 *  * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 *  * body（object类型数据）
 */

public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger MESSAGE_COUNTER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {

    }
}
