package com.alibaba.dubbo.remoting.transport.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;

@Sharable
public class Netty4Handler extends ChannelDuplexHandler {
	private final Map<String, Channel> channels; // <ip:port,
	private final URL url;
	private final ChannelHandler handler;

	public Netty4Handler(URL url, ChannelHandler handler) {
		if (url == null) {
			throw new IllegalArgumentException("url == null");
		}
		if (handler == null) {
			throw new IllegalArgumentException("handler == null");
		}
		this.url = url;
		this.handler = handler;
		this.channels = new ConcurrentHashMap<String, Channel>();
	}

	public Map<String, Channel> getChannels() {
		return channels;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		Netty4Channel channel = Netty4Channel.getOrAddChannel(ctx.channel(),
				url, handler);
		try {
			if (channel != null) {
				channels.put(NetUtils.toAddressString((InetSocketAddress) ctx
						.channel().remoteAddress()), channel);
			}
			handler.connected(channel);
		} finally {
			Netty4Channel.removeChannelIfDisconnected(ctx.channel());
		}

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		Netty4Channel channel = Netty4Channel.getOrAddChannel(ctx.channel(),
				url, handler);
		try {
			channels.remove(NetUtils.toAddressString((InetSocketAddress) ctx
					.channel().remoteAddress()));
			handler.disconnected(channel);
		} finally {
			Netty4Channel.removeChannelIfDisconnected(ctx.channel());
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Netty4Channel channel = Netty4Channel.getOrAddChannel(ctx.channel(),
				url, handler);
		try {
			handler.received(channel, msg);
		} finally {
			Netty4Channel.removeChannelIfDisconnected(ctx.channel());
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		ctx.writeAndFlush(msg, promise);
		Netty4Channel channel = Netty4Channel.getOrAddChannel(ctx.channel(),
				url, handler);
		try {
			handler.sent(channel, msg);
		} finally {
			Netty4Channel.removeChannelIfDisconnected(ctx.channel());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		Netty4Channel channel = Netty4Channel.getOrAddChannel(ctx.channel(),
				url, handler);
		try {
			handler.caught(channel, cause.getCause());
		} finally {
			Netty4Channel.removeChannelIfDisconnected(ctx.channel());
		}
	}
}
