package com.xxl.rpc.sample.server;

import com.xxl.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.xxl.rpc.core.remoting.provider.RpcProviderFactory;
import com.xxl.rpc.core.serialize.impl.HessianSerializer;
import com.xxl.rpc.sample.api.DemoService;
import com.xxl.rpc.sample.server.service.DemoServiceImpl;

import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2018-10-21 20:48:40
 */
public class RpcServerApplication {

    public static void main(String[] args) throws Exception {

        // init
        RpcProviderFactory providerFactory = new RpcProviderFactory()
                .setServer(NettyServer.class)
                .setSerializer(HessianSerializer.class)
                .setCorePoolSize(-1)
                .setMaxPoolSize(-1)
                .setIp(null)
                .setPort(7080)
                .setAccessToken(null)
                .setServiceRegistry(null)
                .setServiceRegistryParam(null);

        // add services
        providerFactory.addService(DemoService.class.getName(), null, new DemoServiceImpl());

        // start
        providerFactory.start();

        while (!Thread.currentThread().isInterrupted()) {
            TimeUnit.HOURS.sleep(1);
        }

        // stop
        providerFactory.stop();

    }

}
