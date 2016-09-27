package com.alibaba.dubbo.remoting.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractChannel;

public class Netty4Channel extends AbstractChannel {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Netty4Channel.class);
	private Channel channel;
	private static final Map<Channel, Netty4Channel> CHANNELS = new ConcurrentHashMap<Channel, Netty4Channel>();
	private final Map<String, Object> attributes;

	public Netty4Channel(Channel channel, URL url, ChannelHandler handler) {
		super(url, handler);
		if (channel == null) {
			throw new IllegalArgumentException("netty channel == null;");
		}
		this.channel = channel;
		attributes = new ConcurrentHashMap<String, Object>();
	}

	public static Netty4Channel getOrAddChannel(Channel channel, URL url,
			ChannelHandler handler) {
		if (channel == null) {
			return null;
		}
		Netty4Channel ret = CHANNELS.get(channel);
		if (ret == null) {
			Netty4Channel nc = new Netty4Channel(channel, url, handler);
			if (channel.isActive()) {
				ret = CHANNELS.putIfAbsent(channel, nc);
			}
			if (ret == null) {
				ret = nc;
			}
		}
		return ret;
	}

	public static void removeChannelIfDisconnected(Channel channel) {
		if (channel != null && !channel.isActive()) {
			CHANNELS.remove(channel);
		}
	}

	@Override
	public void send(Object message, boolean sent) throws RemotingException {
		super.send(message, sent);

		boolean success = true;
		int timeout = 0;
		try {
			ChannelFuture future = channel.write(message);
			if (sent) {
				timeout = getUrl().getPositiveParameter(Constants.TIMEOUT_KEY,
						Constants.DEFAULT_TIMEOUT);
				success = future.await(timeout);
			}
			Throwable cause = future.cause();
			if (cause != null) {
				throw cause;
			}
		} catch (Throwable e) {
			throw new RemotingException(this, "Failed to send message "
					+ message + " to " + getRemoteAddress() + ", cause: "
					+ e.getMessage(), e);
		}

		if (!success) {
			throw new RemotingException(this, "Failed to send message "
					+ message + " to " + getRemoteAddress() + "in timeout("
					+ timeout + "ms) limit");
		}
	}

	@Override
	public void close() {
		try {
			super.close();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}
		try {
			removeChannelIfDisconnected(channel);
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}
		try {
			attributes.clear();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}
		try {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Close netty channel " + channel);
			}
			channel.close();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) channel.remoteAddress();
	}

	@Override
	public boolean isConnected() {
		return channel.isActive();
	}

	@Override
	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (value == null)
			attributes.remove(key);
		else
			attributes.put(key, value);
	}

	@Override
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) channel.localAddress();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Netty4Channel other = (Netty4Channel) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Netty4Channel [channel=" + channel + "]";
	}
}
