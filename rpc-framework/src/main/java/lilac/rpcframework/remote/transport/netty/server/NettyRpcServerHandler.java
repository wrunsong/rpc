package lilac.rpcframework.remote.transport.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lilac.rpcframework.enums.CompressType;
import lilac.rpcframework.enums.SerializationType;
import lilac.rpcframework.factory.SingletonFactory;
import lilac.rpcframework.remote.dto.RpcMessage;
import lilac.rpcframework.remote.dto.RpcRequest;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.remote.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import static lilac.rpcframework.remote.constant.RpcConstant.*;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler requestHandler;
    private int idleCounter = 0;

    @Value("${lilac.rpc.max.idle.times:1}")
    private static int MAX_IDLE_TIMES;
    @Value("${lilac.rpc.serialize.type:Hessian}")
    private static String codecType;
    @Value("${lilac.rpc.compress.type:gzip}")
    private static String compressType;

    public NettyRpcServerHandler() {
        this.requestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                byte messageType = ((RpcMessage) msg).getMessageType();
                // response message
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationType.getCodeByType(codecType));
                rpcMessage.setCompress(CompressType.getCodeByName(compressType));

                if (messageType == HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(PONG);
                } else {
                    idleCounter = 0;
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    Object handledResult = requestHandler.handle(rpcRequest);
                    rpcMessage.setMessageType(RESPONSE_TYPE);

                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> success = RpcResponse.success(handledResult, rpcRequest.getRequestId());
                        rpcMessage.setData(success);
                    } else {
                        RpcResponse<Object> fail = RpcResponse.fail();
                        rpcMessage.setData(fail);
                        log.error("Process request [{}] error!", rpcRequest.getRequestId());
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }  catch (Exception e) {
            log.error("Process request [{}] error, {} ", msg, e.getMessage());
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                idleCounter++;
                if (idleCounter > MAX_IDLE_TIMES) {
                    log.info("idle check happen more than max times, so close the connection");
                    ctx.close();
                }
            }
        } else  {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Process request [{}] error, {} ", ctx, cause.getMessage());
        ctx.close();
    }
}
