package com.xxl.rpc.core.remoting.net.impl.netty.server;

import com.xxl.rpc.core.remoting.net.params.Beat;
import com.xxl.rpc.core.remoting.net.params.RpcRequest;
import com.xxl.rpc.core.remoting.net.params.RpcResponse;
import com.xxl.rpc.core.remoting.provider.RpcProviderFactory;
import com.xxl.rpc.core.util.ThrowableUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * netty server handler
 *
 * @author mzj 2015-10-29 20:07:37
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private RpcProviderFactory xxlRpcProviderFactory;
    private ThreadPoolExecutor serverHandlerPool;

    public NettyServerHandler(final RpcProviderFactory xxlRpcProviderFactory, final ThreadPoolExecutor serverHandlerPool) {
        this.xxlRpcProviderFactory = xxlRpcProviderFactory;
        this.serverHandlerPool = serverHandlerPool;
    }


    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final RpcRequest xxlRpcRequest) throws Exception {

        // filter beat
        if (Beat.BEAT_ID.equalsIgnoreCase(xxlRpcRequest.getRequestId())){
            logger.debug(">>>>>>>>>>> rpc provider netty server read beat-ping.");
            return;
        }

        // do invoke
        try {
            serverHandlerPool.execute(new Runnable() {
                @Override
                public void run() {
                    // invoke + response
                    RpcResponse xxlRpcResponse = xxlRpcProviderFactory.invokeService(xxlRpcRequest);

                    ctx.writeAndFlush(xxlRpcResponse);
                }
            });
        } catch (Exception e) {
            // catch error
            RpcResponse xxlRpcResponse = new RpcResponse();
            xxlRpcResponse.setRequestId(xxlRpcRequest.getRequestId());
            xxlRpcResponse.setErrorMsg(ThrowableUtil.toString(e));

            ctx.writeAndFlush(xxlRpcResponse);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.error(">>>>>>>>>>> rpc provider netty server caught exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            ctx.channel().close();      // beat 3N, close if idle
            logger.debug(">>>>>>>>>>> rpc provider netty server close an idle channel.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
