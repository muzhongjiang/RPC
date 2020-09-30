package com.xxl.rpc.core.remoting.net.params;

import lombok.Data;

import java.io.Serializable;

/**
 * response
 *
 * @author xuxueli 2015-10-29 19:39:54
 */
@Data
public class RpcResponse implements Serializable{

	private String requestId;
    private String errorMsg;
    private Object result;



}
