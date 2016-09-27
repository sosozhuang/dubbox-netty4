package com.alibaba.dubbo.remoting.transport.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.transport.AbstractServer;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelHandlers;

public class Netty4Server extends AbstractServer implements Server {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Netty4Server.class);
	private ServerBootstrap bootstrap;
	private ChannelFuture channelFuture;
	private Map<String, Channel> channels;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;

	public Netty4Server(URL url, ChannelHandler handler)
			throws RemotingException {
		super(url, ChannelHandlers.wrap(handler,
				ExecutorUtil.setThreadName(url, SERVER_THREAD_POOL_NAME)));
	}

	@Override
	public boolean isBound() {
		return channelFuture.channel().isActive();
	}

	@Override
	public Collection<Channel> getChannels() {
		Collection<Channel> chs = new HashSet<Channel>();
		for (Channel channel : channels.values()) {
			if (channel.isConnected()) {
				chs.add(channel);
			} else {
				channels.remove(NetUtils.toAddressString(channel
						.getRemoteAddress()));
			}
		}
		return chs;
	}

	@Override
	public Channel getChannel(InetSocketAddress remoteAddress) {
		return channels.get(NetUtils.toAddressString(remoteAddress));
	}

	@Override
	protected void doOpen() throws Throwable {
		Netty4Helper.setNettyLoggerFactory();

		final Netty4Handler handler = new Netty4Handler(getUrl(), this);
		channels = handler.getChannels();

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch)
							throws Exception {
						ChannelPipeline p = ch.pipeline();
						Netty4CodecAdapter adapter = new Netty4CodecAdapter(
								getCodec(), getUrl(), Netty4Server.this);
						p.addLast("decoder", adapter.getDecoder());
						p.addLast("encoder", adapter.getEncoder());
						p.addLast("handler", handler);
					}

				});

		channelFuture = bootstrap.bind(getBindAddress()).sync();
	}

	@Override
	protected void doClose() throws Throwable {
		try {
			if (channelFuture != null) {
				channelFuture.channel().close();
			}
		} catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
		try {
			Collection<com.alibaba.dubbo.remoting.Channel> channels = getChannels();
			if (channels != null && channels.size() > 0) {
				for (com.alibaba.dubbo.remoting.Channel channel : channels) {
					try {
						channel.close();
					} catch (Throwable e) {
						LOGGER.warn(e.getMessage(), e);
					}
				}
			}
		} catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
		try {
			if (bossGroup != null) {
				bossGroup.shutdownGracefully();
			}
		} catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
		try {
			if (workerGroup != null) {
				workerGroup.shutdownGracefully();
			}
		} catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
		try {
			if (channels != null) {
				channels.clear();
			}
		} catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

}
