package com.xxl.rpc.core.remoting.invoker.reference;

import com.xxl.rpc.core.remoting.invoker.RpcInvokerFactory;
import com.xxl.rpc.core.remoting.invoker.call.CallType;
import com.xxl.rpc.core.remoting.invoker.call.RpcInvokeCallback;
import com.xxl.rpc.core.remoting.invoker.call.RpcInvokeFuture;
import com.xxl.rpc.core.remoting.invoker.generic.RpcGenericService;
import com.xxl.rpc.core.remoting.invoker.route.LoadBalance;
import com.xxl.rpc.core.remoting.net.Client;
import com.xxl.rpc.core.remoting.net.impl.netty.client.NettyClient;
import com.xxl.rpc.core.remoting.net.params.RpcFutureResponse;
import com.xxl.rpc.core.remoting.net.params.RpcRequest;
import com.xxl.rpc.core.remoting.net.params.RpcResponse;
import com.xxl.rpc.core.remoting.provider.RpcProviderFactory;
import com.xxl.rpc.core.serialize.Serializer;
import com.xxl.rpc.core.serialize.impl.HessianSerializer;
import com.xxl.rpc.core.util.ClassUtil;
import com.xxl.rpc.core.util.RpcException;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * rpc reference bean, use by api
 *
 * @author xuxueli 2015-10-29 20:18:32
 */
@Slf4j
@Data
@Accessors(chain = true)
public class RpcReferenceBean {
	// [tips01: save 30ms/100invoke. why why why??? with this log, it can save lots of time.]

	// ---------------------- config ----------------------

	private Class<? extends Client> client = NettyClient.class;
	private Class<? extends Serializer> serializer = HessianSerializer.class;
	private CallType callType = CallType.SYNC;
	private LoadBalance loadBalance = LoadBalance.ROUND;

	private Class<?> iface = null;
	private String version = null;

	private long timeout = 1000;

	private String address = null;
	private String accessToken = null;

	private RpcInvokeCallback invokeCallback = null;

	private RpcInvokerFactory invokerFactory = null;


	// ---------------------- initClient ----------------------

	private Client clientInstance = null;
	private Serializer serializerInstance = null;

	public RpcReferenceBean initClient() throws Exception {

		// valid
		if (this.client == null) {
			throw new RpcException("rpc reference client missing.");
		}
		if (this.serializer == null) {
			throw new RpcException("rpc reference serializer missing.");
		}
		if (this.callType==null) {
			throw new RpcException("rpc reference callType missing.");
		}
		if (this.loadBalance==null) {
			throw new RpcException("rpc reference loadBalance missing.");
		}
		if (this.iface==null) {
			throw new RpcException("rpc reference iface missing.");
		}
		if (this.timeout < 0) {
			this.timeout = 0;
		}
		if (this.invokerFactory == null) {
			this.invokerFactory = RpcInvokerFactory.getInstance();
		}

		// init serializerInstance
		this.serializerInstance = serializer.newInstance();

		// init Client
		clientInstance = client.newInstance();
		clientInstance.init(this);

		return this;
	}


	// ---------------------- util ----------------------

	public Object getObject() throws Exception {

		// initClient
		initClient();

		// newProxyInstance
		return Proxy.newProxyInstance(Thread.currentThread()
				.getContextClassLoader(), new Class[] { iface },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

						// method param
						String className = method.getDeclaringClass().getName();	// iface.getName()
						String varsion_ = version;
						String methodName = method.getName();
						Class<?>[] parameterTypes = method.getParameterTypes();
						Object[] parameters = args;

						// filter for generic
						if (className.equals(RpcGenericService.class.getName()) && methodName.equals("invoke")) {

							Class<?>[] paramTypes = null;
							if (args[3]!=null) {
								String[] paramTypes_str = (String[]) args[3];
								if (paramTypes_str.length > 0) {
									paramTypes = new Class[paramTypes_str.length];
									for (int i = 0; i < paramTypes_str.length; i++) {
										paramTypes[i] = ClassUtil.resolveClass(paramTypes_str[i]);
									}
								}
							}

							className = (String) args[0];
							varsion_ = (String) args[1];
							methodName = (String) args[2];
							parameterTypes = paramTypes;
							parameters = (Object[]) args[4];
						}

						// filter method like "Object.toString()"
						if (className.equals(Object.class.getName())) {
							log.info(">>>>>>>>>>> rpc proxy class-method not support [{}#{}]", className, methodName);
							throw new RpcException("rpc proxy class-method not support");
						}

						// address
						String finalAddress = address;
						if (finalAddress==null || finalAddress.trim().length()==0) {
							if (invokerFactory!=null && invokerFactory.getRegister()!=null) {
								// discovery
								String serviceKey = RpcProviderFactory.makeServiceKey(className, varsion_);
								TreeSet<String> addressSet = invokerFactory.getRegister().discovery(serviceKey);
								// load balance
								if (addressSet==null || addressSet.size()==0) {
									// pass
								} else if (addressSet.size()==1) {
									finalAddress = addressSet.first();
								} else {
									finalAddress = loadBalance.xxlRpcInvokerRouter.route(serviceKey, addressSet);
								}

							}
						}
						if (finalAddress==null || finalAddress.trim().length()==0) {
							throw new RpcException("rpc reference bean["+ className +"] address empty");
						}

						// request
						RpcRequest xxlRpcRequest = new RpcRequest();
	                    xxlRpcRequest.setRequestId(UUID.randomUUID().toString());
	                    xxlRpcRequest.setCreateMillisTime(System.currentTimeMillis());
	                    xxlRpcRequest.setAccessToken(accessToken);
	                    xxlRpcRequest.setClassName(className);
	                    xxlRpcRequest.setMethodName(methodName);
	                    xxlRpcRequest.setParameterTypes(parameterTypes);
	                    xxlRpcRequest.setParameters(parameters);
	                    xxlRpcRequest.setVersion(version);

	                    // send
						if (CallType.SYNC == callType) {
							// future-response set
							RpcFutureResponse futureResponse = new RpcFutureResponse(invokerFactory, xxlRpcRequest, null);
							try {
								// do invoke
								clientInstance.asyncSend(finalAddress, xxlRpcRequest);

								// future get
								RpcResponse xxlRpcResponse = futureResponse.get(timeout, TimeUnit.MILLISECONDS);
								if (xxlRpcResponse.getErrorMsg() != null) {
									throw new RpcException(xxlRpcResponse.getErrorMsg());
								}
								return xxlRpcResponse.getResult();
							} catch (Exception e) {
								log.info(">>>>>>>>>>> rpc, invoke error, address:{}, RpcRequest{}", finalAddress, xxlRpcRequest);

								throw (e instanceof RpcException)?e:new RpcException(e);
							} finally{
								// future-response remove
								futureResponse.removeInvokerFuture();
							}
						} else if (CallType.FUTURE == callType) {
							// future-response set
							RpcFutureResponse futureResponse = new RpcFutureResponse(invokerFactory, xxlRpcRequest, null);
                            try {
								// invoke future set
								RpcInvokeFuture invokeFuture = new RpcInvokeFuture(futureResponse);
								RpcInvokeFuture.setFuture(invokeFuture);

                                // do invoke
								clientInstance.asyncSend(finalAddress, xxlRpcRequest);

                                return null;
                            } catch (Exception e) {
								log.info(">>>>>>>>>>> rpc, invoke error, address:{}, RpcRequest{}", finalAddress, xxlRpcRequest);

								// future-response remove
								futureResponse.removeInvokerFuture();

								throw (e instanceof RpcException)?e:new RpcException(e);
                            }

						} else if (CallType.CALLBACK == callType) {

							// get callback
							RpcInvokeCallback finalInvokeCallback = invokeCallback;
							RpcInvokeCallback threadInvokeCallback = RpcInvokeCallback.getCallback();
							if (threadInvokeCallback != null) {
								finalInvokeCallback = threadInvokeCallback;
							}
							if (finalInvokeCallback == null) {
								throw new RpcException("rpc RpcInvokeCallback（CallType="+ CallType.CALLBACK.name() +"） cannot be null.");
							}

							// future-response set
							RpcFutureResponse futureResponse = new RpcFutureResponse(invokerFactory, xxlRpcRequest, finalInvokeCallback);
							try {
								clientInstance.asyncSend(finalAddress, xxlRpcRequest);
							} catch (Exception e) {
								log.info(">>>>>>>>>>> rpc, invoke error, address:{}, RpcRequest{}", finalAddress, xxlRpcRequest);

								// future-response remove
								futureResponse.removeInvokerFuture();

								throw (e instanceof RpcException)?e:new RpcException(e);
							}

							return null;
						} else if (CallType.ONEWAY == callType) {
							clientInstance.asyncSend(finalAddress, xxlRpcRequest);
                            return null;
                        } else {
							throw new RpcException("rpc callType["+ callType +"] invalid");
						}

					}
				});
	}

}
