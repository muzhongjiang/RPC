package com.xxl.rpc.core.util;

/**
 * @author mzj 2018-10-20 23:00:40
 */
public class RpcException extends RuntimeException {

    public RpcException(String msg) {
        super(msg);
    }

    public RpcException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

}
