package com.alibaba.dubbo.remoting.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;

public class Netty4ChannelBufferFactory implements ChannelBufferFactory {
	private static final Netty4ChannelBufferFactory INSTANCE = new Netty4ChannelBufferFactory();

	private Netty4ChannelBufferFactory() {}
	
	public static ChannelBufferFactory getInstance() {
		return INSTANCE;
	}
	
	public static ChannelBuffer getBuffer(ByteBuf buf) {
		return new Netty4ChannelBuffer(buf);
	}
	
	@Override
	public ChannelBuffer getBuffer(int capacity) {
		return new Netty4ChannelBuffer(Unpooled.buffer(capacity));
	}

	@Override
	public ChannelBuffer getBuffer(byte[] array, int offset, int length) {
		ByteBuf buf = Unpooled.buffer(length);
		buf.writeBytes(array, offset, length);
		return new Netty4ChannelBuffer(buf);
	}

	@Override
	public ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
		return new Netty4ChannelBuffer(Unpooled.wrappedBuffer(nioBuffer));
	}

}
