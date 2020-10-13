package com.xxl.rpc.core.remoting.provider;

import com.xxl.rpc.core.registry.Register;
import com.xxl.rpc.core.remoting.net.Server;
import com.xxl.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.xxl.rpc.core.remoting.net.params.BaseCallback;
import com.xxl.rpc.core.remoting.net.params.RpcRequest;
import com.xxl.rpc.core.remoting.net.params.RpcResponse;
import com.xxl.rpc.core.serialize.Serializer;
import com.xxl.rpc.core.serialize.impl.JavaSerializer;
import com.xxl.rpc.core.util.IpUtil;
import com.xxl.rpc.core.util.NetUtil;
import com.xxl.rpc.core.util.RpcException;
import com.xxl.rpc.core.util.ThrowableUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * provider
 *
 * @author mzj 2015-10-31 22:54:27
 */
@Slf4j
@Data
@Accessors(chain = true)
public class RpcProviderFactory {

	// ---------------------- config ----------------------

	private Class<? extends Server> server = NettyServer.class;
	private Class<? extends Serializer> serializer = JavaSerializer.class;

	private int corePoolSize = 60;
	private int maxPoolSize = 300;

	private String ip = null;					// server ip, for registry
	private int port = 7080;					// server default port
	private String registryAddress;				// default use registryAddress to registry , otherwise use ip:port if registryAddress is null
	private String accessToken = null;

	private Class<? extends Register> serviceRegistry = null;
	private Map<String, String> serviceRegistryParam = null;



	// ---------------------- start / stop ----------------------

	private Server serverInstance;
	private Serializer serializerInstance;
	private Register registerInstance;

	public void start() throws Exception {

		// valid
		if (this.server == null) {
			throw new RpcException("rpc provider server missing.");
		}
		if (this.serializer==null) {
			throw new RpcException("rpc provider serializer missing.");
		}
		if (!(this.corePoolSize>0 && this.maxPoolSize>0 && this.maxPoolSize>=this.corePoolSize)) {
			this.corePoolSize = 60;
			this.maxPoolSize = 300;
		}
		if (this.ip == null) {
			this.ip = IpUtil.getIp();
		}
		if (this.port <= 0) {
			this.port = 7080;
		}
		if (this.registryAddress==null || this.registryAddress.trim().length()==0) {
			this.registryAddress = IpUtil.getIpPort(this.ip, this.port);
		}
		if (NetUtil.isPortUsed(this.port)) {
			throw new RpcException("rpc provider port["+ this.port +"] is used.");
		}

		// init serializerInstance
		this.serializerInstance = serializer.newInstance();

		// start server
		serverInstance = server.newInstance();
		serverInstance.setStartedCallback(new BaseCallback() {		// serviceRegistry started
			@Override
			public void run() throws Exception {
				// start registry
				if (serviceRegistry != null) {
					registerInstance = serviceRegistry.newInstance();
					registerInstance.start(serviceRegistryParam);
					if (serviceData.size() > 0) {
						registerInstance.registry(serviceData.keySet(), registryAddress);
					}
				}
			}
		});
		serverInstance.setStopedCallback(new BaseCallback() {		// serviceRegistry stoped
			@Override
			public void run() {
				// stop registry
				if (registerInstance != null) {
					if (serviceData.size() > 0) {
						registerInstance.remove(serviceData.keySet(), registryAddress);
					}
					registerInstance.stop();
					registerInstance = null;
				}
			}
		});
		serverInstance.start(this);
	}

	public void  stop() throws Exception {
		// stop server
		serverInstance.stop();
	}


	// ---------------------- server invoke ----------------------

	/**
	 * init local rpc service map
	 */
	private Map<String, Object> serviceData = new HashMap<String, Object>();
	public Map<String, Object> getServiceData() {
		return serviceData;
	}

	/**
	 * make service key
	 */
	public static String makeServiceKey(String iface, String version){
		String serviceKey = iface;
		if (version!=null && version.trim().length()>0) {
			serviceKey += "#".concat(version);
		}
		return serviceKey;
	}

	/**
	 * add service
	 */
	public void addService(String iface, String version, Object serviceBean){
		String serviceKey = makeServiceKey(iface, version);
		serviceData.put(serviceKey, serviceBean);
		log.info(">>>>>>>>>>> rpc, provider factory add service success. serviceKey = {}, serviceBean = {}", serviceKey, serviceBean.getClass());
	}

	/**
	 * invoke service
	 */
	public RpcResponse invokeService(RpcRequest xxlRpcRequest) {

		//  make response
		RpcResponse xxlRpcResponse = new RpcResponse();
		xxlRpcResponse.setRequestId(xxlRpcRequest.getRequestId());

		// match service bean
		String serviceKey = makeServiceKey(xxlRpcRequest.getClassName(), xxlRpcRequest.getVersion());
		Object serviceBean = serviceData.get(serviceKey);

		// valid
		if (serviceBean == null) {
			xxlRpcResponse.setErrorMsg("The serviceKey["+ serviceKey +"] not found.");
			return xxlRpcResponse;
		}

		if (System.currentTimeMillis() - xxlRpcRequest.getCreateMillisTime() > 3*60*1000) {
			xxlRpcResponse.setErrorMsg("The timestamp difference between admin and executor exceeds the limit.");
			return xxlRpcResponse;
		}
		if (accessToken!=null && accessToken.trim().length()>0 && !accessToken.trim().equals(xxlRpcRequest.getAccessToken())) {
			xxlRpcResponse.setErrorMsg("The access token[" + xxlRpcRequest.getAccessToken() + "] is wrong.");
			return xxlRpcResponse;
		}

		try {
			// invoke
			Class<?> serviceClass = serviceBean.getClass();
			String methodName = xxlRpcRequest.getMethodName();
			Class<?>[] parameterTypes = xxlRpcRequest.getParameterTypes();
			Object[] parameters = xxlRpcRequest.getParameters();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
			Object result = method.invoke(serviceBean, parameters);

			/*FastClass serviceFastClass = FastClass.create(serviceClass);
			FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
			Object result = serviceFastMethod.invoke(serviceBean, parameters);*/

			xxlRpcResponse.setResult(result);
		} catch (Throwable t) {
			// catch error
			log.error("rpc provider invokeService error.", t);
			xxlRpcResponse.setErrorMsg(ThrowableUtil.toString(t));
		}

		return xxlRpcResponse;
	}

}
