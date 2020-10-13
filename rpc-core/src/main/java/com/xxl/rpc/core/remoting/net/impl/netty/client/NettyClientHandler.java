package com.xxl.rpc.core.remoting.net.impl.netty.client;

import com.xxl.rpc.core.remoting.invoker.RpcInvokerFactory;
import com.xxl.rpc.core.remoting.net.params.Beat;
import com.xxl.rpc.core.remoting.net.params.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * rpc netty client handler
 *
 * @author mzj 2015-10-31 18:00:27
 */
@Slf4j
@AllArgsConstructor
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

	private RpcInvokerFactory xxlRpcInvokerFactory;
	private NettyConnectClient nettyConnectClient;


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse xxlRpcResponse) throws Exception {

		// notify response
		xxlRpcInvokerFactory.notifyInvokerFuture(xxlRpcResponse.getRequestId(), xxlRpcResponse);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(">>>>>>>>>>> rpc netty client caught exception", cause);
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent){
			/*ctx.channel().close();      // close idle channel
			log.debug(">>>>>>>>>>> rpc netty client close an idle channel.");*/

			nettyConnectClient.send(Beat.BEAT_PING);	// beat N, close if fail(may throw error)
			log.debug(">>>>>>>>>>> rpc netty client send beat-ping.");

		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

}
