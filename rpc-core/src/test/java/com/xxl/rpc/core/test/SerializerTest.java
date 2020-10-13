package com.xxl.rpc.core.test;

import com.xxl.rpc.core.serialize.Serializer;
import com.xxl.rpc.core.serialize.impl.JavaSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mzj 2015-10-30 21:02:55
 */
public class SerializerTest {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Serializer serializer = JavaSerializer.class.newInstance();
        System.out.println(serializer);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("aaa", "111");
            map.put("bbb", "222");
            System.out.println(serializer.deserialize(serializer.serialize("ddddddd"), String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
