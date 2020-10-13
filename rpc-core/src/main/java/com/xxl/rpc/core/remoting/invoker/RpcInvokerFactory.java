package com.xxl.rpc.core.remoting.invoker;

import com.xxl.rpc.core.registry.Register;
import com.xxl.rpc.core.registry.impl.LocalRegister;
import com.xxl.rpc.core.remoting.net.params.BaseCallback;
import com.xxl.rpc.core.remoting.net.params.RpcFutureResponse;
import com.xxl.rpc.core.remoting.net.params.RpcResponse;
import com.xxl.rpc.core.util.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * rpc invoker factory, init service-registry
 *
 * @author mzj 2018-10-19
 */
@Slf4j
public class RpcInvokerFactory {

    // ---------------------- default instance ----------------------

    private static volatile RpcInvokerFactory instance = new RpcInvokerFactory(LocalRegister.class, null);
    public static RpcInvokerFactory getInstance() {
        return instance;
    }


    // ---------------------- config ----------------------

    private Class<? extends Register> serviceRegistryClass;          // class.forname
    private Map<String, String> serviceRegistryParam;


    public RpcInvokerFactory() {
    }
    public RpcInvokerFactory(Class<? extends Register> serviceRegistryClass, Map<String, String> serviceRegistryParam) {
        this.serviceRegistryClass = serviceRegistryClass;
        this.serviceRegistryParam = serviceRegistryParam;
    }


    // ---------------------- start / stop ----------------------

    public void start() throws Exception {
        // start registry
        if (serviceRegistryClass != null) {
            register = serviceRegistryClass.newInstance();
            register.start(serviceRegistryParam);
        }
    }

    public void  stop() throws Exception {
        // stop registry
        if (register != null) {
            register.stop();
        }

        // stop callback
        if (stopCallbackList.size() > 0) {
            for (BaseCallback callback: stopCallbackList) {
                try {
                    callback.run();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        // stop CallbackThreadPool
        stopCallbackThreadPool();
    }


    // ---------------------- service registry ----------------------

    private Register register;
    public Register getRegister() {
        return register;
    }


    // ---------------------- service registry ----------------------

    private List<BaseCallback> stopCallbackList = new ArrayList<BaseCallback>();

    public void addStopCallBack(BaseCallback callback){
        stopCallbackList.add(callback);
    }


    // ---------------------- future-response pool ----------------------

    // RpcFutureResponseFactory

    private ConcurrentMap<String, RpcFutureResponse> futureResponsePool = new ConcurrentHashMap<String, RpcFutureResponse>();
    public void setInvokerFuture(String requestId, RpcFutureResponse futureResponse){
        futureResponsePool.put(requestId, futureResponse);
    }
    public void removeInvokerFuture(String requestId){
        futureResponsePool.remove(requestId);
    }
    public void notifyInvokerFuture(String requestId, final RpcResponse xxlRpcResponse){

        // get
        final RpcFutureResponse futureResponse = futureResponsePool.get(requestId);
        if (futureResponse == null) {
            return;
        }

        // notify
        if (futureResponse.getInvokeCallback()!=null) {

            // callback type
            try {
                executeResponseCallback(new Runnable() {
                    @Override
                    public void run() {
                        if (xxlRpcResponse.getErrorMsg() != null) {
                            futureResponse.getInvokeCallback().onFailure(new RpcException(xxlRpcResponse.getErrorMsg()));
                        } else {
                            futureResponse.getInvokeCallback().onSuccess(xxlRpcResponse.getResult());
                        }
                    }
                });
            }catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {

            // other nomal type
            futureResponse.setResponse(xxlRpcResponse);
        }

        // do remove
        futureResponsePool.remove(requestId);

    }


    // ---------------------- response callback ThreadPool ----------------------

    private ThreadPoolExecutor responseCallbackThreadPool = null;
    public void executeResponseCallback(Runnable runnable){

        if (responseCallbackThreadPool == null) {
            synchronized (this) {
                if (responseCallbackThreadPool == null) {
                    responseCallbackThreadPool = new ThreadPoolExecutor(
                            10,
                            100,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(1000),
                            new ThreadFactory() {
                                @Override
                                public Thread newThread(Runnable r) {
                                    return new Thread(r, "rpc, RpcInvokerFactory-responseCallbackThreadPool-" + r.hashCode());
                                }
                            },
                            new RejectedExecutionHandler() {
                                @Override
                                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                    throw new RpcException("rpc Invoke Callback Thread pool is EXHAUSTED!");
                                }
                            });		// default maxThreads 300, minThreads 60
                }
            }
        }
        responseCallbackThreadPool.execute(runnable);
    }
    public void stopCallbackThreadPool() {
        if (responseCallbackThreadPool != null) {
            responseCallbackThreadPool.shutdown();
        }
    }

}
