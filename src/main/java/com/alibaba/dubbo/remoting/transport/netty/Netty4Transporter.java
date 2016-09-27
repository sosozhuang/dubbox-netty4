package com.alibaba.dubbo.remoting.transport.netty;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.Transporter;

public class Netty4Transporter implements Transporter {

	@Override
	public Server bind(URL url, ChannelHandler handler)
			throws RemotingException {
		return new Netty4Server(url, handler);
	}

	@Override
	public Client connect(URL url, ChannelHandler handler)
			throws RemotingException {
		return new Netty4Client(url, handler);
	}
}
