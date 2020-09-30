package com.xxl.rpc.core.remoting.invoker.reference.impl;

import com.xxl.rpc.core.remoting.invoker.reference.RpcReferenceBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * rpc reference bean, use by spring xml and annotation (for spring)
 *
 * @author xuxueli 2015-10-29 20:18:32
 */
public class RpcSpringReferenceBean implements FactoryBean<Object>, InitializingBean {


    // ---------------------- util ----------------------

    private RpcReferenceBean xxlRpcReferenceBean;

    @Override
    public void afterPropertiesSet() {

        // init config
        this.xxlRpcReferenceBean = new RpcReferenceBean();
    }


    @Override
    public Object getObject() throws Exception {
        return xxlRpcReferenceBean.getObject();
    }

    @Override
    public Class<?> getObjectType() {
        return xxlRpcReferenceBean.getIface();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }


    /**
     *	public static <T> ClientProxy ClientProxy<T> getFuture(Class<T> type) {
     *		<T> ClientProxy proxy = (<T>) new ClientProxy();
     *		return proxy;
     *	}
     */


}
