package com.xxl.rpc.sample.server.conf;

import com.xxl.rpc.core.registry.impl.RpcAdminRegister;
import com.xxl.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.xxl.rpc.core.remoting.provider.impl.RpcSpringProviderFactory;
import com.xxl.rpc.core.serialize.impl.JavaSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * rpc provider config
 *
 * @author mzj 2018-10-19
 */
@Configuration
public class RpcProviderConfig {
    private Logger logger = LoggerFactory.getLogger(RpcProviderConfig.class);

    @Value("${rpc.remoting.port}")
    private int port;

    @Value("${rpc.registry.xxlrpcadmin.address}")
    private String address;

    @Value("${rpc.registry.xxlrpcadmin.env}")
    private String env;

    @Bean
    public RpcSpringProviderFactory xxlRpcSpringProviderFactory() {

        RpcSpringProviderFactory providerFactory = new RpcSpringProviderFactory();
        providerFactory.setServer(NettyServer.class);
        providerFactory.setSerializer(JavaSerializer.class);
        providerFactory.setCorePoolSize(-1);
        providerFactory.setMaxPoolSize(-1);
        providerFactory.setIp(null);
        providerFactory.setPort(port);
        providerFactory.setAccessToken(null);
        providerFactory.setServiceRegistry(RpcAdminRegister.class);
        providerFactory.setServiceRegistryParam(new HashMap<String, String>() {{
            put(RpcAdminRegister.ADMIN_ADDRESS, address);
            put(RpcAdminRegister.ENV, env);
        }});

        logger.info(">>>>>>>>>>> rpc provider config init finish.");
        return providerFactory;
    }

}
