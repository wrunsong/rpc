package lilac.rpcframework.remote.transport.netty.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lilac.rpcframework.constants.Constants;
import lilac.rpcframework.enums.CompressType;
import lilac.rpcframework.enums.SerializationType;
import lilac.rpcframework.remote.dto.RpcMessage;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.remote.transport.netty.client.utils.ChannelProvider;
import lilac.rpcframework.remote.transport.netty.client.utils.UnProcessedRequests;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static lilac.rpcframework.remote.constant.RpcConstant.*;

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private static final String compressType = Constants.COMPRESS_TYPE;
    private static final String codecType = Constants.CODEC_TYPE;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("channelRead, msg: {}", msg);
        try {
            if (msg instanceof RpcMessage rpcMessage) {
                byte messageType = rpcMessage.getMessageType();
                if (messageType == HEARTBEAT_RESPONSE_TYPE) {
                    log.info("receive heartbeat response: {}", rpcMessage.getData());
                } else if (messageType == RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) rpcMessage.getData();
                    UnProcessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.debug("userEventTriggered, evt: {}", evt);
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.WRITER_IDLE) {
                Channel channel = ChannelProvider.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                if (channel == null) {
                    // TODO 加一个补救措施，尝试重新建立连接
                    log.error("Channel is null");
                    return;
                }
                RpcMessage rpcMessage = RpcMessage.builder()
                        .codec(SerializationType.getCodeByType(codecType))
                        .messageType(HEARTBEAT_REQUEST_TYPE)
                        .data(PING)
                        .compress(CompressType.getCodeByName(compressType))
                        .build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client caught exception: {}", cause.getMessage());
        ctx.close();
    }
}
