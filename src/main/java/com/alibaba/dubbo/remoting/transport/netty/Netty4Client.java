package com.alibaba.dubbo.remoting.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractClient;

public class Netty4Client extends AbstractClient {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Netty4Client.class);
	private Bootstrap bootstrap;
	private volatile io.netty.channel.Channel channel;
	private NioEventLoopGroup group;

	public Netty4Client(URL url, ChannelHandler handler)
			throws RemotingException {
		super(url, wrapChannelHandler(url, handler));
	}

	@Override
	protected void doOpen() throws Throwable {
		Netty4Helper.setNettyLoggerFactory();
		bootstrap = new Bootstrap();
		group = new NioEventLoopGroup();
		final Netty4Handler handler = new Netty4Handler(getUrl(), this);
		bootstrap.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout())
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch)
							throws Exception {
						ChannelPipeline p = ch.pipeline();
						Netty4CodecAdapter adapter = new Netty4CodecAdapter(
								getCodec(), getUrl(), Netty4Client.this);
						p.addLast("decoder", adapter.getDecoder());
						p.addLast("encoder", adapter.getEncoder());
						p.addLast("handler", handler);
					}
				});

	}

	@Override
	protected void doClose() throws Throwable {
		group.shutdownGracefully();
	}

	@Override
	protected void doConnect() throws Throwable {
		long start = System.currentTimeMillis();
		ChannelFuture future = bootstrap.connect(getConnectAddress()).sync();
		try {
			boolean ret = future.awaitUninterruptibly(getConnectTimeout(),
					TimeUnit.MILLISECONDS);

			if (ret && future.isSuccess()) {
				io.netty.channel.Channel newChannel = future.channel();
				newChannel.config().setAutoRead(false);
				try {
					// copy ref first
					io.netty.channel.Channel oldChannel = Netty4Client.this.channel;
					// 关闭旧的连接
					if (oldChannel != null) {
						try {
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("Close old netty channel "
										+ oldChannel
										+ " on create new netty channel "
										+ newChannel);
							}
							oldChannel.close();
						} finally {
							Netty4Channel
									.removeChannelIfDisconnected(oldChannel);
						}
					}
				} finally {
					if (Netty4Client.this.isClosed()) {
						try {
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("Close new netty channel "
										+ newChannel
										+ ", because the client closed.");
							}
							newChannel.close();
						} finally {
							Netty4Client.this.channel = null;
							Netty4Channel
									.removeChannelIfDisconnected(newChannel);
						}
					} else {
						Netty4Client.this.channel = newChannel;
					}
				}
			} else if (future.cause() != null) {
				throw new RemotingException(this, "client(url: " + getUrl()
						+ ") failed to connect to server " + getRemoteAddress()
						+ ", error message is:" + future.cause().getMessage(),
						future.cause());
			} else {
				throw new RemotingException(this, "client(url: " + getUrl()
						+ ") failed to connect to server " + getRemoteAddress()
						+ " client-side timeout " + getConnectTimeout()
						+ "ms (elapsed: "
						+ (System.currentTimeMillis() - start)
						+ "ms) from netty client " + NetUtils.getLocalHost()
						+ " using dubbo version " + Version.getVersion());
			}
		} finally {
			if (!isConnected()) {
				future.cancel(true);
			}
		}
	}

	@Override
	protected void doDisConnect() throws Throwable {
		try {
			Netty4Channel.removeChannelIfDisconnected(channel);
		} catch (Throwable t) {
			LOGGER.warn(t.getMessage());
		}
	}

	@Override
	protected Channel getChannel() {
		io.netty.channel.Channel copyChannel = channel;
		if (copyChannel == null || !copyChannel.isActive())
			return null;
		return Netty4Channel.getOrAddChannel(copyChannel, getUrl(), this);
	}
}
