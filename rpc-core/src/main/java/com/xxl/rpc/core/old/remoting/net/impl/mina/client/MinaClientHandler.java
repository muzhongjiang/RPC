//package com.xxl.rpc.core.remoting.net.impl.mina.client;
//
//import com.xxl.rpc.core.remoting.invoker.RpcInvokerFactory;
//import com.xxl.rpc.core.remoting.net.params.Beat;
//import com.xxl.rpc.core.remoting.net.params.RpcResponse;
//import org.apache.mina.core.service.IoHandlerAdapter;
//import org.apache.mina.core.session.IdleStatus;
//import org.apache.mina.core.session.IoSession;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * rpc mina handler
// * @author xuxueli 2015-11-14 18:55:19
// */
//public class MinaClientHandler extends IoHandlerAdapter {
//	private static final Logger logger = LoggerFactory.getLogger(MinaClientHandler.class);
//
//
//	private RpcInvokerFactory xxlRpcInvokerFactory;
//	public MinaClientHandler(final RpcInvokerFactory xxlRpcInvokerFactory) {
//		this.xxlRpcInvokerFactory = xxlRpcInvokerFactory;
//	}
//
//
//	@Override
//	public void messageReceived(IoSession session, Object message) throws Exception {
//        RpcResponse xxlRpcResponse = (RpcResponse) message;
//
//        // filter beat
//        if (Beat.BEAT_ID.equalsIgnoreCase(xxlRpcResponse.getRequestId())){
//            return;
//        }
//
//        // notify response
//        xxlRpcInvokerFactory.notifyInvokerFuture(xxlRpcResponse.getRequestId(), xxlRpcResponse);
//	}
//
//	@Override
//	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
//		//super.exceptionCaught(session, cause);
//		logger.error(">>>>>>>>>>> rpc mina client caught exception:", cause);
//		session.closeOnFlush();
//	}
//
//	@Override
//	public void sessionCreated(IoSession session) throws Exception {
//		//super.sessionCreated(session);
//		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10*60);
//	}
//
//	@Override
//	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
//		//super.sessionIdle(session, status);
//		if(status == IdleStatus.BOTH_IDLE){
//			session.closeOnFlush();
//			logger.debug(">>>>>>>>>>> rpc mina client close an idle session.");
//		}
//	}
//
//}
