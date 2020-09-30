package com.xxl.rpc.sample.client.conf;

import com.xxl.rpc.core.registry.impl.RpcAdminRegister;
import com.xxl.rpc.core.remoting.invoker.impl.RpcSpringInvokerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * rpc invoker config
 *
 * @author xuxueli 2018-10-19
 */
@Slf4j
@Configuration
public class RpcInvokerConfig {

    @Value("${rpc.registry.xxlrpcadmin.address}")
    private String address;

    @Value("${rpc.registry.xxlrpcadmin.env}")
    private String env;


    @Bean
    public RpcSpringInvokerFactory xxlJobExecutor() {

        RpcSpringInvokerFactory invokerFactory = new RpcSpringInvokerFactory();
        invokerFactory.setServiceRegistryClass(RpcAdminRegister.class);
        invokerFactory.setServiceRegistryParam(new HashMap<String, String>(){{
            put(RpcAdminRegister.ADMIN_ADDRESS, address);
            put(RpcAdminRegister.ENV, env);
        }});

        log.info(">>>>>>>>>>> rpc invoker config init finish.");
        return invokerFactory;
    }

}
