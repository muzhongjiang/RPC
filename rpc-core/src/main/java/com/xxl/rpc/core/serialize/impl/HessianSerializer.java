package com.xxl.rpc.core.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.xxl.rpc.core.serialize.Serializer;
import com.xxl.rpc.core.util.RpcException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * hessian serialize
 * @author xuxueli 2015-9-26 02:53:29
 */
public class HessianSerializer extends Serializer {

	@Override
	public <T> byte[] serialize(T obj){
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Hessian2Output ho = new Hessian2Output(os);
		try {
			ho.writeObject(obj);
			ho.flush();
			byte[] result = os.toByteArray();
			return result;
		} catch (IOException e) {
			throw new RpcException(e);
		} finally {
			try {
				ho.close();
			} catch (IOException e) {
				throw new RpcException(e);
			}
			try {
				os.close();
			} catch (IOException e) {
				throw new RpcException(e);
			}
		}

	}

	@Override
	public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Hessian2Input hi = new Hessian2Input(is);
		try {
			Object result = hi.readObject();
			return result;
		} catch (IOException e) {
			throw new RpcException(e);
		} finally {
			try {
				hi.close();
			} catch (Exception e) {
				throw new RpcException(e);
			}
			try {
				is.close();
			} catch (IOException e) {
				throw new RpcException(e);
			}
		}
	}

}
