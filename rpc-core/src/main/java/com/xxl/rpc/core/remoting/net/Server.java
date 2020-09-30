package com.xxl.rpc.core.remoting.net;

import com.xxl.rpc.core.remoting.net.params.BaseCallback;
import com.xxl.rpc.core.remoting.provider.RpcProviderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * server
 *
 * @author xuxueli 2015-11-24 20:59:49
 */
@Slf4j
public abstract class Server {

	private BaseCallback startedCallback;
	private BaseCallback stopedCallback;

	public void setStartedCallback(BaseCallback startedCallback) {
		this.startedCallback = startedCallback;
	}

	public void setStopedCallback(BaseCallback stopedCallback) {
		this.stopedCallback = stopedCallback;
	}


	/**
	 * start server
	 *
	 * @param xxlRpcProviderFactory
	 * @throws Exception
	 */
	public abstract void start(final RpcProviderFactory xxlRpcProviderFactory) throws Exception;

	/**
	 * callback when started
	 */
	public void onStarted() {
		if (startedCallback != null) {
			try {
				startedCallback.run();
			} catch (Exception e) {
				log.error(">>>>>>>>>>> rpc, server startedCallback error.", e);
			}
		}
	}

	/**
	 * stop server
	 *
	 * @throws Exception
	 */
	public abstract void stop() throws Exception;

	/**
	 * callback when stoped
	 */
	public void onStoped() {
		if (stopedCallback != null) {
			try {
				stopedCallback.run();
			} catch (Exception e) {
				log.error(">>>>>>>>>>> rpc, server stopedCallback error.", e);
			}
		}
	}

}
