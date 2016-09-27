package com.alibaba.dubbo.remoting.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;
import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;

public class Netty4CodecAdapter {
	private final ChannelHandler encoder;
	private final ChannelHandler decoder;
	private Codec2 codec;
	private URL url;
	private com.alibaba.dubbo.remoting.ChannelHandler handler;
	private int bufferSize;

	public Netty4CodecAdapter(Codec2 codec, URL url,
			com.alibaba.dubbo.remoting.ChannelHandler handler) {
		this.codec = codec;
		this.url = url;
		this.handler = handler;
		int b = url.getPositiveParameter(Constants.BUFFER_KEY,
				Constants.DEFAULT_BUFFER_SIZE);
		this.bufferSize = b >= Constants.MIN_BUFFER_SIZE
				&& b <= Constants.MAX_BUFFER_SIZE ? b
				: Constants.DEFAULT_BUFFER_SIZE;
		encoder = new InternalEncoder();
		decoder = new InternalDecoder();
	}

	public ChannelHandler getEncoder() {
		return encoder;
	}

	public ChannelHandler getDecoder() {
		return decoder;
	}

	@Sharable
	private class InternalEncoder extends MessageToByteEncoder<Object> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
				throws Exception {
			ChannelBuffer buffer = Netty4ChannelBufferFactory.getBuffer(out);
			Netty4Channel channel = Netty4Channel.getOrAddChannel(
					ctx.channel(), url, handler);
			try {
				codec.encode(channel, buffer, msg);
			} finally {
				Netty4Channel.removeChannelIfDisconnected(ctx.channel());
			}
		}

	}

	private class InternalDecoder extends ByteToMessageDecoder {
		private ChannelBuffer buffer = Netty4ChannelBufferFactory.getInstance()
				.getBuffer(bufferSize);

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			ctx.fireChannelRead(cause);
		}

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in,
				List<Object> out) throws Exception {
			if (!in.isReadable())
				return;

			buffer.writeBytes(Netty4ChannelBufferFactory.getBuffer(in));

			Netty4Channel channel = Netty4Channel.getOrAddChannel(
					ctx.channel(), url, handler);
			Object object;
			int saveReaderIndex;

			try {
				while (buffer.readable()) {
					saveReaderIndex = buffer.readerIndex();
					try {
						object = codec.decode(channel, buffer);
					} catch (IOException e) {
						buffer.clear();
						throw e;
					}
					if (object == Codec2.DecodeResult.NEED_MORE_INPUT) {
						buffer.readerIndex(saveReaderIndex);
						break;
					} else {
						if (saveReaderIndex == buffer.readerIndex()) {
							buffer.clear();
							throw new IOException("Decode without read data.");
						}
						if (object != null) {
							out.add(object);
						}
					}
				}

			} finally {
				if (buffer.readable()) {
					buffer.discardReadBytes();
				} else {
					buffer.clear();
				}
				Netty4Channel.removeChannelIfDisconnected(ctx.channel());
			}
		}

	}
}
