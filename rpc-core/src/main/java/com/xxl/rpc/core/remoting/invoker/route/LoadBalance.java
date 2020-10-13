package com.xxl.rpc.core.remoting.invoker.route;

import com.xxl.rpc.core.remoting.invoker.route.impl.*;

/**
 * @author mzj 2018-12-04
 */
public enum LoadBalance {

    RANDOM(new RpcLoadBalanceRandomStrategy()),
    ROUND(new RpcLoadBalanceRoundStrategy()),
    LRU(new RpcLoadBalanceLRUStrategy()),
    LFU(new RpcLoadBalanceLFUStrategy()),
    CONSISTENT_HASH(new RpcLoadBalanceConsistentHashStrategy());

    public final RpcLoadBalance xxlRpcInvokerRouter;

    private LoadBalance(RpcLoadBalance xxlRpcInvokerRouter) {
        this.xxlRpcInvokerRouter = xxlRpcInvokerRouter;
    }

    public static LoadBalance match(String name, LoadBalance defaultRouter) {
        for (LoadBalance item : LoadBalance.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultRouter;
    }

}
