package com.xxl.rpc.core.remoting.invoker.impl;

import com.xxl.rpc.core.registry.Register;
import com.xxl.rpc.core.remoting.invoker.RpcInvokerFactory;
import com.xxl.rpc.core.remoting.invoker.annotation.RpcReference;
import com.xxl.rpc.core.remoting.invoker.reference.RpcReferenceBean;
import com.xxl.rpc.core.remoting.provider.RpcProviderFactory;
import com.xxl.rpc.core.util.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * rpc invoker factory, init service-registry and spring-bean by annotation (for spring)
 *
 * @author mzj 2018-10-19
 */
@Slf4j
public class RpcSpringInvokerFactory extends InstantiationAwareBeanPostProcessorAdapter implements InitializingBean, DisposableBean, BeanFactoryAware {

    // ---------------------- config ----------------------

    private Class<? extends Register> serviceRegistryClass;          // class.forname
    private Map<String, String> serviceRegistryParam;


    public void setServiceRegistryClass(Class<? extends Register> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }


    // ---------------------- util ----------------------

    private RpcInvokerFactory xxlRpcInvokerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        // start invoker factory
        xxlRpcInvokerFactory = new RpcInvokerFactory(serviceRegistryClass, serviceRegistryParam);
        xxlRpcInvokerFactory.start();
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {

        // collection
        final Set<String> serviceKeyList = new HashSet<>();

        // parse RpcReferenceBean
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(RpcReference.class)) {
                    // valid
                    Class iface = field.getType();
                    if (!iface.isInterface()) {
                        throw new RpcException("rpc, reference(RpcReference) must be interface.");
                    }

                    RpcReference rpcReference = field.getAnnotation(RpcReference.class);

                    // init reference bean
                    RpcReferenceBean referenceBean = new RpcReferenceBean()
                            .setClient(rpcReference.client())
                            .setSerializer(rpcReference.serializer())
                            .setCallType(rpcReference.callType())
                            .setLoadBalance(rpcReference.loadBalance())
                            .setIface(iface)
                            .setVersion(rpcReference.version())
                            .setTimeout(rpcReference.timeout())
                            .setAddress(rpcReference.address())
                            .setAccessToken(rpcReference.accessToken())
                            .setInvokeCallback(null)
                            .setInvokerFactory(xxlRpcInvokerFactory);


                    // get proxyObj
                    Object serviceProxy = null;
                    try {
                        serviceProxy = referenceBean.getObject();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    // set bean
                    field.setAccessible(true);
                    field.set(bean, serviceProxy);

                    log.info(">>>>>>>>>>> rpc, invoker factory init reference bean success. serviceKey = {}, bean.field = {}.{}",
                            RpcProviderFactory.makeServiceKey(iface.getName(), rpcReference.version()), beanName, field.getName());

                    // collection
                    String serviceKey = RpcProviderFactory.makeServiceKey(iface.getName(), rpcReference.version());
                    serviceKeyList.add(serviceKey);

                }
            }
        });

        // mult discovery
        if (xxlRpcInvokerFactory.getRegister() != null) {
            try {
                xxlRpcInvokerFactory.getRegister().discovery(serviceKeyList);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return super.postProcessAfterInstantiation(bean, beanName);
    }


    @Override
    public void destroy() throws Exception {

        // stop invoker factory
        xxlRpcInvokerFactory.stop();
    }

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
