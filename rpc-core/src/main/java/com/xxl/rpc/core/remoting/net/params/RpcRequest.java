package com.xxl.rpc.core.remoting.net.params;

import lombok.Data;

import java.io.Serializable;

/**
 * request
 *
 * @author xuxueli 2015-10-29 19:39:12
 */
@Data
public class RpcRequest implements Serializable{

	private String requestId;
	private long createMillisTime;
	private String accessToken;

    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

	private String version;


}
