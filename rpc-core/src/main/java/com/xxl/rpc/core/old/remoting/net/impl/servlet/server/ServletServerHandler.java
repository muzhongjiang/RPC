//package com.xxl.rpc.core.remoting.net.impl.servlet.server;
//
//import com.xxl.rpc.core.remoting.net.params.RpcRequest;
//import com.xxl.rpc.core.remoting.net.params.RpcResponse;
//import com.xxl.rpc.core.remoting.provider.RpcProviderFactory;
//import com.xxl.rpc.core.util.ThrowableUtil;
//import com.xxl.rpc.core.util.RpcException;
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
// * servlet
// *
// * @author xuxueli 2015-11-24 22:25:15
// */
//public class ServletServerHandler {
//    private static Logger logger = LoggerFactory.getLogger(ServletServerHandler.class);
//
//    private RpcProviderFactory xxlRpcProviderFactory;
//    public ServletServerHandler(RpcProviderFactory xxlRpcProviderFactory) {
//        this.xxlRpcProviderFactory = xxlRpcProviderFactory;
//    }
//
//    /**
//     * handle servlet request
//     */
//    public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//
//        if ("/services".equals(target)) {	// services mapping
//
//            StringBuffer stringBuffer = new StringBuffer("<ui>");
//            for (String serviceKey: xxlRpcProviderFactory.getServiceData().keySet()) {
//                stringBuffer.append("<li>").append(serviceKey).append(": ").append(xxlRpcProviderFactory.getServiceData().get(serviceKey)).append("</li>");
//            }
//            stringBuffer.append("</ui>");
//
//            writeResponse(response, stringBuffer.toString().getBytes());
//            return;
//        } else {	// default remoting mapping
//
//            // request parse
//            RpcRequest xxlRpcRequest = null;
//            try {
//
//                xxlRpcRequest = parseRequest(request);
//            } catch (Exception e) {
//                writeResponse(response, ThrowableUtil.toString(e).getBytes());
//                return;
//            }
//
//            // invoke
//            RpcResponse xxlRpcResponse = xxlRpcProviderFactory.invokeService(xxlRpcRequest);
//
//            // response-serialize + response-write
//            byte[] responseBytes = xxlRpcProviderFactory.getSerializer().serialize(xxlRpcResponse);
//            writeResponse(response, responseBytes);
//        }
//
//    }
//
//    /**
//     * write response
//     */
//    private void writeResponse(HttpServletResponse response, byte[] responseBytes) throws IOException {
//
//        response.setContentType("text/html;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_OK);
//
//        OutputStream out = response.getOutputStream();
//        out.write(responseBytes);
//        out.flush();
//    }
//
//    /**
//     * parse request
//     */
//    private RpcRequest parseRequest(HttpServletRequest request) throws Exception {
//        // deserialize request
//        byte[] requestBytes = readBytes(request);
//        if (requestBytes == null || requestBytes.length==0) {
//            throw new RpcException("rpc request data is empty.");
//        }
//        RpcRequest rpcRpcRequest = (RpcRequest) xxlRpcProviderFactory.getSerializer().deserialize(requestBytes, RpcRequest.class);
//        return rpcRpcRequest;
//    }
//
//    /**
//     * read bytes from http request
//     *
//     * @param request
//     * @return
//     * @throws IOException
//     */
//    public static final byte[] readBytes(HttpServletRequest request) throws IOException {
//        request.setCharacterEncoding("UTF-8");
//        int contentLen = request.getContentLength();
//        InputStream is = request.getInputStream();
//        if (contentLen > 0) {
//            int readLen = 0;
//            int readLengthThisTime = 0;
//            byte[] message = new byte[contentLen];
//            try {
//                while (readLen != contentLen) {
//                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
//                    if (readLengthThisTime == -1) {
//                        break;
//                    }
//                    readLen += readLengthThisTime;
//                }
//                return message;
//            } catch (IOException e) {
//                logger.error(e.getMessage(), e);
//            }
//        }
//        return new byte[] {};
//    }
//
//
//
//}
