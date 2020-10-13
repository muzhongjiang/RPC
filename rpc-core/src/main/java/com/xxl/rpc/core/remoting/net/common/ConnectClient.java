package com.xxl.rpc.core.remoting.net.common;

import com.xxl.rpc.core.remoting.invoker.RpcInvokerFactory;
import com.xxl.rpc.core.remoting.invoker.reference.RpcReferenceBean;
import com.xxl.rpc.core.remoting.net.params.BaseCallback;
import com.xxl.rpc.core.remoting.net.params.RpcRequest;
import com.xxl.rpc.core.serialize.Serializer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author mzj 2018-10-19
 */
public abstract class ConnectClient {

    // ---------------------- iface ----------------------

    public abstract void init(String address, final Serializer serializer, final RpcInvokerFactory xxlRpcInvokerFactory) throws Exception;

    public abstract void close();

    public abstract boolean isValidate();

    public abstract void send(RpcRequest xxlRpcRequest) throws Exception ;


    // ---------------------- client pool map ----------------------

    /**
     * async send
     */
    public static void asyncSend(RpcRequest xxlRpcRequest, String address,
                                 Class<? extends ConnectClient> connectClientImpl,
                                 final RpcReferenceBean xxlRpcReferenceBean
    ) throws Exception {

        // client pool	[tips03 : may save 35ms/100invoke if move it to constructor, but it is necessary. cause by ConcurrentHashMap.get]
        ConnectClient clientPool = ConnectClient.getPool(address, connectClientImpl, xxlRpcReferenceBean);

        try {
            // do invoke
            clientPool.send(xxlRpcRequest);
        } catch (Exception e) {
            throw e;
        }

    }

    private static volatile ConcurrentMap<String, ConnectClient> connectClientMap;        // (static) alread addStopCallBack
    private static volatile ConcurrentMap<String, Object> connectClientLockMap = new ConcurrentHashMap<>();
    private static ConnectClient getPool(String address, Class<? extends ConnectClient> connectClientImpl, final RpcReferenceBean xxlRpcReferenceBean) throws Exception {

        // init base compont, avoid repeat init
        if (connectClientMap == null) {
            synchronized (ConnectClient.class) {
                if (connectClientMap == null) {
                    // init
                    connectClientMap = new ConcurrentHashMap<String, ConnectClient>();
                    // stop callback
                    xxlRpcReferenceBean.getInvokerFactory().addStopCallBack(new BaseCallback() {
                        @Override
                        public void run() throws Exception {
                            if (connectClientMap.size() > 0) {
                                for (String key: connectClientMap.keySet()) {
                                    ConnectClient clientPool = connectClientMap.get(key);
                                    clientPool.close();
                                }
                                connectClientMap.clear();
                            }
                        }
                    });
                }
            }
        }

        // get-valid client
        ConnectClient connectClient = connectClientMap.get(address);
        if (connectClient!=null && connectClient.isValidate()) {
            return connectClient;
        }

        // lock
        Object clientLock = connectClientLockMap.get(address);
        if (clientLock == null) {
            connectClientLockMap.putIfAbsent(address, new Object());
            clientLock = connectClientLockMap.get(address);
        }

        // remove-create new client
        synchronized (clientLock) {

            // get-valid client, avlid repeat
            connectClient = connectClientMap.get(address);
            if (connectClient!=null && connectClient.isValidate()) {
                return connectClient;
            }

            // remove old
            if (connectClient != null) {
                connectClient.close();
                connectClientMap.remove(address);
            }

            // set pool
            ConnectClient connectClient_new = connectClientImpl.newInstance();
            try {
                connectClient_new.init(address, xxlRpcReferenceBean.getSerializerInstance(), xxlRpcReferenceBean.getInvokerFactory());
                connectClientMap.put(address, connectClient_new);
            } catch (Exception e) {
                connectClient_new.close();
                throw e;
            }

            return connectClient_new;
        }

    }

}
