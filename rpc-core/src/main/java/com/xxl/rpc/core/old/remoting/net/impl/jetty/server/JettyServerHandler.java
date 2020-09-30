//package com.xxl.rpc.core.remoting.net.impl.jetty.server;
//
//import com.xxl.rpc.core.remoting.net.params.RpcRequest;
//import com.xxl.rpc.core.remoting.net.params.RpcResponse;
//import com.xxl.rpc.core.remoting.provider.RpcProviderFactory;
//import com.xxl.rpc.core.util.ThrowableUtil;
//import com.xxl.rpc.core.util.RpcException;
//import org.eclipse.jetty.server.Request;
//import org.eclipse.jetty.server.handler.AbstractHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
///**
// * jetty handler
// * @author xuxueli 2015-11-19 22:32:36
// */
//public class JettyServerHandler extends AbstractHandler {
//	private static Logger logger = LoggerFactory.getLogger(JettyServerHandler.class);
//
//
//	private RpcProviderFactory xxlRpcProviderFactory;
//	public JettyServerHandler(final RpcProviderFactory xxlRpcProviderFactory) {
//		this.xxlRpcProviderFactory = xxlRpcProviderFactory;
//	}
//
//
//	@Override
//	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//
//		if ("/services".equals(target)) {	// services mapping
//
//			StringBuffer stringBuffer = new StringBuffer("<ui>");
//			for (String serviceKey: xxlRpcProviderFactory.getServiceData().keySet()) {
//				stringBuffer.append("<li>").append(serviceKey).append(": ").append(xxlRpcProviderFactory.getServiceData().get(serviceKey)).append("</li>");
//			}
//			stringBuffer.append("</ui>");
//
//			writeResponse(baseRequest, response, stringBuffer.toString().getBytes());
//			return;
//		} else {	// default remoting mapping
//
//			// request parse
//			RpcRequest xxlRpcRequest = null;
//			try {
//
//				xxlRpcRequest = parseRequest(request);
//			} catch (Exception e) {
//				writeResponse(baseRequest, response, ThrowableUtil.toString(e).getBytes());
//				return;
//			}
//
//			// invoke
//			RpcResponse xxlRpcResponse = xxlRpcProviderFactory.invokeService(xxlRpcRequest);
//
//			// response-serialize + response-write
//			byte[] responseBytes = xxlRpcProviderFactory.getSerializer().serialize(xxlRpcResponse);
//			writeResponse(baseRequest, response, responseBytes);
//		}
//
//	}
//
//	/**
//	 * write response
//	 */
//	private void writeResponse(Request baseRequest, HttpServletResponse response, byte[] responseBytes) throws IOException {
//
//		response.setContentType("text/html;charset=UTF-8");
//		response.setStatus(HttpServletResponse.SC_OK);
//		baseRequest.setHandled(true);
//
//		OutputStream out = response.getOutputStream();
//		out.write(responseBytes);
//		out.flush();
//	}
//
//	/**
//	 * parse request
//	 */
//	private RpcRequest parseRequest(HttpServletRequest request) throws Exception {
//		// deserialize request
//		byte[] requestBytes = readBytes(request);
//		if (requestBytes == null || requestBytes.length==0) {
//			throw new RpcException("rpc request data is empty.");
//		}
//		RpcRequest rpcRpcRequest = (RpcRequest) xxlRpcProviderFactory.getSerializer().deserialize(requestBytes, RpcRequest.class);
//		return rpcRpcRequest;
//	}
//
//	/**
//	 * read bytes from http request
//	 *
//	 * @param request
//	 * @return
//	 * @throws IOException
//	 */
//	public static final byte[] readBytes(HttpServletRequest request) throws IOException {
//		request.setCharacterEncoding("UTF-8");
//		int contentLen = request.getContentLength();
//		InputStream is = request.getInputStream();
//		if (contentLen > 0) {
//			int readLen = 0;
//			int readLengthThisTime = 0;
//			byte[] message = new byte[contentLen];
//			try {
//				while (readLen != contentLen) {
//					readLengthThisTime = is.read(message, readLen, contentLen - readLen);
//					if (readLengthThisTime == -1) {
//						break;
//					}
//					readLen += readLengthThisTime;
//				}
//				return message;
//			} catch (IOException e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
//		return new byte[] {};
//	}
//
//}
