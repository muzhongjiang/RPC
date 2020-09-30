package com.xxl.rpc.sample.client;

import com.xxl.rpc.core.remoting.invoker.RpcInvokerFactory;
import com.xxl.rpc.core.remoting.invoker.call.CallType;
import com.xxl.rpc.core.remoting.invoker.call.RpcInvokeCallback;
import com.xxl.rpc.core.remoting.invoker.call.RpcInvokeFuture;
import com.xxl.rpc.core.remoting.invoker.reference.RpcReferenceBean;
import com.xxl.rpc.core.remoting.invoker.route.LoadBalance;
import com.xxl.rpc.core.remoting.net.impl.netty.client.NettyClient;
import com.xxl.rpc.core.serialize.impl.HessianSerializer;
import com.xxl.rpc.sample.api.DemoService;
import com.xxl.rpc.sample.api.dto.UserDTO;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2018-10-21 20:48:40
 */
public class RpcClientAplication {

    public static void main(String[] args) throws Exception {

		/*String serviceKey = RpcProviderFactory.makeServiceKey(DemoService.class.getName(), null);
		RpcInvokerFactory.getInstance().getServiceRegistry().registry(new HashSet<String>(Arrays.asList(serviceKey)), "127.0.0.1:7080");*/

        // test
        testSYNC();
        testFUTURE();
        testCALLBACK();
        testONEWAY();

        TimeUnit.SECONDS.sleep(2);

        // stop client invoker factory (default by getInstance, exist inner thread, need destory)
        RpcInvokerFactory.getInstance().stop();

    }


    /**
     * CallType.SYNC
     */
    public static void testSYNC() throws Exception {
        // init client
        RpcReferenceBean referenceBean = new RpcReferenceBean()
                .setClient(NettyClient.class)
                .setSerializer(HessianSerializer.class)
                .setCallType(CallType.SYNC)
                .setLoadBalance(LoadBalance.ROUND)
                .setIface(DemoService.class)
                .setVersion(null)
                .setTimeout(500)
                .setAddress("127.0.0.1:7080")
                .setAccessToken(null)
                .setInvokeCallback(null)
                .setInvokerFactory(null);

        DemoService demoService = (DemoService) referenceBean.getObject();

        // test
        UserDTO userDTO = demoService.sayHi("[SYNC]jack");
        System.out.println(userDTO);
    }


    /**
     * CallType.FUTURE
     */
    public static void testFUTURE() throws Exception {
        // client
        RpcReferenceBean referenceBean = new RpcReferenceBean()
                .setClient(NettyClient.class)
                .setSerializer(HessianSerializer.class)
                .setCallType(CallType.FUTURE) //
                .setLoadBalance(LoadBalance.ROUND)
                .setIface(DemoService.class)
                .setVersion(null)
                .setTimeout(500)
                .setAddress("127.0.0.1:7080")
                .setAccessToken(null)
                .setInvokeCallback(null)
                .setInvokerFactory(null);


        DemoService demoService = (DemoService) referenceBean.getObject();

        // test
        demoService.sayHi("[FUTURE]jack");
        Future<UserDTO> userDTOFuture = RpcInvokeFuture.getFuture(UserDTO.class);
        UserDTO userDTO = userDTOFuture.get();

        System.out.println(userDTO.toString());
    }


    /**
     * CallType.CALLBACK
     */
    public static void testCALLBACK() throws Exception {
        // client
        RpcReferenceBean referenceBean = new RpcReferenceBean()
                .setClient(NettyClient.class)
                .setSerializer(HessianSerializer.class)
                .setCallType(CallType.CALLBACK) //
                .setLoadBalance(LoadBalance.ROUND)
                .setIface(DemoService.class)
                .setVersion(null)
                .setTimeout(500)
                .setAddress("127.0.0.1:7080")
                .setAccessToken(null)
                .setInvokeCallback(null)
                .setInvokerFactory(null);

        DemoService demoService = (DemoService) referenceBean.getObject();

        // test
        RpcInvokeCallback.setCallback(new RpcInvokeCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                System.out.println(result);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        demoService.sayHi("[CALLBACK]jack");
    }


    /**
     * CallType.ONEWAY
     */
    public static void testONEWAY() throws Exception {
        // client t
        // client
        RpcReferenceBean referenceBean = new RpcReferenceBean()
                .setClient(NettyClient.class)
                .setSerializer(HessianSerializer.class)
                .setCallType(CallType.ONEWAY) //
                .setLoadBalance(LoadBalance.ROUND)
                .setIface(DemoService.class)
                .setVersion(null)
                .setTimeout(500)
                .setAddress("127.0.0.1:7080")
                .setAccessToken(null)
                .setInvokeCallback(null)
                .setInvokerFactory(null);


        DemoService demoService = (DemoService) referenceBean.getObject();

        // test
        demoService.sayHi("[ONEWAY]jack");
    }

}
