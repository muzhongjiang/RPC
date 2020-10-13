package com.xxl.rpc.core.remoting.net.impl.netty.codec;

import com.xxl.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * encoder
 *
 * @author mzj 2015-10-29 19:43:00
 */
@Slf4j
@AllArgsConstructor
public class NettyEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass;
    private Serializer serializer;


    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data = serializer.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
