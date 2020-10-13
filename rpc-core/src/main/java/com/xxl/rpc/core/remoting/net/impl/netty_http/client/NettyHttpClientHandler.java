package com.xxl.rpc.core.remoting.net.impl.netty_http.client;

import com.xxl.rpc.core.remoting.invoker.RpcInvokerFactory;
import com.xxl.rpc.core.remoting.net.params.Beat;
import com.xxl.rpc.core.remoting.net.params.RpcResponse;
import com.xxl.rpc.core.serialize.Serializer;
import com.xxl.rpc.core.util.RpcException;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * netty_http
 *
 * @author mzj 2015-11-24 22:25:15
 */
@Slf4j
@AllArgsConstructor
public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private RpcInvokerFactory xxlRpcInvokerFactory;
    private Serializer serializer;
    private NettyHttpConnectClient nettyHttpConnectClient;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        // valid status
        if (!HttpResponseStatus.OK.equals(msg.status())) {
            throw new RpcException("rpc response status invalid.");
        }

        // response parse
        byte[] responseBytes = ByteBufUtil.getBytes(msg.content());

        // valid length
        if (responseBytes.length == 0) {
            throw new RpcException("rpc response data empty.");
        }

        // response deserialize
        RpcResponse xxlRpcResponse = (RpcResponse) serializer.deserialize(responseBytes, RpcResponse.class);

        // notify response
        xxlRpcInvokerFactory.notifyInvokerFuture(xxlRpcResponse.getRequestId(), xxlRpcResponse);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        log.error(">>>>>>>>>>> rpc netty_http client caught exception", cause);
        ctx.close();
    }

    /*@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // retry
        super.channelInactive(ctx);
    }*/

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            /*ctx.channel().close();      // close idle channel
            log.debug(">>>>>>>>>>> rpc netty_http client close an idle channel.");*/

            nettyHttpConnectClient.send(Beat.BEAT_PING);    // beat N, close if fail(may throw error)
            log.debug(">>>>>>>>>>> rpc netty_http client send beat-ping.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
