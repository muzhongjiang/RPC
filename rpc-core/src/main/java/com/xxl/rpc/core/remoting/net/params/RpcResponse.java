package com.xxl.rpc.core.remoting.net.params;

import lombok.Data;

import java.io.Serializable;

/**
 * response
 */
@Data
public class RpcResponse implements Serializable{

	private String requestId;
    private String errorMsg;
    private Object result;

}
