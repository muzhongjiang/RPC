package com.xxl.rpc.core.serialize.impl;


import com.xxl.rpc.core.serialize.Serializer;
import com.xxl.rpc.core.util.RpcException;
import lombok.val;

import java.io.*;


public class JavaSerializer extends Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        try (
                val byteArrayOutputStream = new ByteArrayOutputStream();//序列化结果OutputStream
                val objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        ) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            byte[] serResult = byteArrayOutputStream.toByteArray();
            return serResult;
        } catch (IOException e) {
            throw new RpcException(e);
        }
    }


    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        try (
                val byteArrayInputStream = new ByteArrayInputStream(bytes);//反序列化数据InputStream
                val objectInputStream = new ObjectInputStream(byteArrayInputStream);
        ) {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException(e);
        }
    }

}
