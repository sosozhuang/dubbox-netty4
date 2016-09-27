package com.alibaba.dubbo.remoting.transport.netty;

import java.text.MessageFormat;

import io.netty.util.internal.logging.AbstractInternalLogger;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

public class Netty4Helper {
	public static void setNettyLoggerFactory() {
		InternalLoggerFactory factory = InternalLoggerFactory.getDefaultFactory();
		if (factory == null) {
			InternalLoggerFactory.setDefaultFactory(new DubboLoggerFactory());
		}
	}
	
	static class DubboLoggerFactory extends InternalLoggerFactory {

        @Override
        public InternalLogger newInstance(String name) {
            return new DubboLogger(name);
        }
    }
	
	static class DubboLogger extends AbstractInternalLogger {
		
		private Logger logger;

		protected DubboLogger(String name) {
			super(name);
			this.logger = LoggerFactory.getLogger(name);
		}

		private static final long serialVersionUID = 1706583887357124636L;

		@Override
		public boolean isTraceEnabled() {
			return logger.isTraceEnabled();
		}

		@Override
		public void trace(String msg) {
			logger.trace(msg);
		}

		@Override
		public void trace(String format, Object arg) {
			logger.trace(MessageFormat.format(format, arg));
		}

		@Override
		public void trace(String format, Object argA, Object argB) {
			logger.trace(MessageFormat.format(format, argA, argB));
		}

		@Override
		public void trace(String format, Object... arguments) {
			logger.trace(MessageFormat.format(format, arguments));
		}

		@Override
		public void trace(String msg, Throwable t) {
			logger.trace(msg, t);
		}

		@Override
		public boolean isDebugEnabled() {
			return logger.isDebugEnabled();
		}

		@Override
		public void debug(String msg) {
			logger.debug(msg);
		}

		@Override
		public void debug(String format, Object arg) {
			logger.debug(MessageFormat.format(format, arg));
		}

		@Override
		public void debug(String format, Object argA, Object argB) {
			logger.debug(MessageFormat.format(format, argA, argB));
		}

		@Override
		public void debug(String format, Object... arguments) {
			logger.debug(MessageFormat.format(format, arguments));
		}

		@Override
		public void debug(String msg, Throwable t) {
			logger.debug(msg, t);
		}

		@Override
		public boolean isInfoEnabled() {
			return logger.isInfoEnabled();
		}

		@Override
		public void info(String msg) {
			logger.info(msg);
		}

		@Override
		public void info(String format, Object arg) {
			logger.info(MessageFormat.format(format, arg));
		}

		@Override
		public void info(String format, Object argA, Object argB) {
			logger.info(MessageFormat.format(format, argA, argB));
		}

		@Override
		public void info(String format, Object... arguments) {
			logger.info(MessageFormat.format(format, arguments));
		}

		@Override
		public void info(String msg, Throwable t) {
			logger.info(msg, t);
		}

		@Override
		public boolean isWarnEnabled() {
			return logger.isWarnEnabled();
		}

		@Override
		public void warn(String msg) {
			logger.warn(msg);
		}

		@Override
		public void warn(String format, Object arg) {
			logger.warn(MessageFormat.format(format, arg));
		}

		@Override
		public void warn(String format, Object... arguments) {
			logger.warn(MessageFormat.format(format, arguments));
		}

		@Override
		public void warn(String format, Object argA, Object argB) {
			logger.warn(MessageFormat.format(format, argA, argB));
		}

		@Override
		public void warn(String msg, Throwable t) {
			logger.warn(msg, t);
		}

		@Override
		public boolean isErrorEnabled() {
			return logger.isErrorEnabled();
		}

		@Override
		public void error(String msg) {
			logger.error(msg);
		}

		@Override
		public void error(String format, Object arg) {
			logger.error(MessageFormat.format(format, arg));
		}

		@Override
		public void error(String format, Object argA, Object argB) {
			logger.error(MessageFormat.format(format, argA, argB));
		}

		@Override
		public void error(String format, Object... arguments) {
			logger.error(MessageFormat.format(format, arguments));
		}

		@Override
		public void error(String msg, Throwable t) {
			logger.error(msg, t);
		}
		
	}
}
