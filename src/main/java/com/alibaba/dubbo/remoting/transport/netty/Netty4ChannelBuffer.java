package com.alibaba.dubbo.remoting.transport.netty;

import static io.netty.util.internal.MathUtil.isOutOfBounds;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffers;

public class Netty4ChannelBuffer implements ChannelBuffer {
	private final ByteBuf buf;

	public Netty4ChannelBuffer(ByteBuf buf) {
		this.buf = buf;
	}

	public ByteBuf getBuf() {
		return buf;
	}

	@Override
	public int compareTo(ChannelBuffer o) {
		return ChannelBuffers.compare(this, o);
	}

	@Override
	public int capacity() {
		return buf.capacity();
	}

	@Override
	public void clear() {
		buf.clear();
	}

	@Override
	public ChannelBuffer copy() {
		return new Netty4ChannelBuffer(buf.copy());
	}

	@Override
	public ChannelBuffer copy(int index, int length) {
		return new Netty4ChannelBuffer(buf.copy(index, length));
	}

	@Override
	public void discardReadBytes() {
		buf.discardReadBytes();
	}

	@Override
	public void ensureWritableBytes(int writableBytes) {
		buf.ensureWritable(writableBytes);
	}

	@Override
	public ChannelBufferFactory factory() {
		return Netty4ChannelBufferFactory.getInstance();
	}

	@Override
	public byte getByte(int index) {
		return buf.getByte(index);
	}

	@Override
	public void getBytes(int index, byte[] dst) {
		buf.getBytes(index, dst);
	}

	@Override
	public void getBytes(int index, byte[] dst, int dstIndex, int length) {
		buf.getBytes(index, dst, dstIndex, length);
	}

	@Override
	public void getBytes(int index, ByteBuffer dst) {
		buf.getBytes(index, dst);
	}

	@Override
	public void getBytes(int index, ChannelBuffer dst) {
		getBytes(index, dst, dst.readableBytes());
	}

	@Override
	public void getBytes(int index, ChannelBuffer dst, int length) {
		getBytes(index, dst, dst.readerIndex(), length);
		dst.writerIndex(dst.writerIndex() + length);
	}

	private void checkDstIndex(int index, int length, int dstIndex,
			int dstCapacity) {
		checkIndex(index, length);
		if (isOutOfBounds(dstIndex, length, dstCapacity)) {
			throw new IndexOutOfBoundsException(String.format(
					"dstIndex: %d, length: %d (expected: range(0, %d))",
					dstIndex, length, dstCapacity));
		}
	}

	@Override
	public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
		checkDstIndex(index, length, dstIndex, dst.capacity());
		if (dst.hasArray()) {
			buf.getBytes(index, dst.array(), dstIndex, length);
		} else {
			byte[] v = new byte[length];
			buf.getBytes(index, v);
			dst.setBytes(dstIndex, v);
		}
	}

	@Override
	public void getBytes(int index, OutputStream dst, int length)
			throws IOException {
		buf.getBytes(index, dst, length);
	}

	@Override
	public boolean isDirect() {
		return buf.isDirect();
	}

	@Override
	public void markReaderIndex() {
		buf.markReaderIndex();
	}

	@Override
	public void markWriterIndex() {
		buf.markWriterIndex();
	}

	@Override
	public boolean readable() {
		return buf.isReadable();
	}

	@Override
	public int readableBytes() {
		return buf.readableBytes();
	}

	@Override
	public byte readByte() {
		return buf.readByte();
	}

	@Override
	public void readBytes(byte[] dst) {
		buf.readBytes(dst);
	}

	@Override
	public void readBytes(byte[] dst, int dstIndex, int length) {
		buf.readBytes(dst, dstIndex, length);
	}

	@Override
	public void readBytes(ByteBuffer dst) {
		buf.readBytes(dst);
	}

	@Override
	public void readBytes(ChannelBuffer dst) {
		readBytes(dst, dst.writableBytes());
	}

	@Override
	public void readBytes(ChannelBuffer dst, int length) {
		if (length > dst.writableBytes()) {
			throw new IndexOutOfBoundsException(
					String.format(
							"length(%d) exceeds dst.writableBytes(%d) where dst is: %s",
							length, dst.writableBytes(), dst));
		}
		readBytes(dst, dst.writerIndex(), length);
		dst.writerIndex(dst.writerIndex() + length);
	}

	private void checkReadableBytes(int minimumReadableBytes) {
		if (minimumReadableBytes < 0) {
			throw new IllegalArgumentException("minimumReadableBytes: "
					+ minimumReadableBytes + " (expected: >= 0)");
		}
		checkReadableBytes0(minimumReadableBytes);
	}

	private void checkReadableBytes0(int minimumReadableBytes) {
		if (readerIndex() > writerIndex() - minimumReadableBytes) {
			throw new IndexOutOfBoundsException(String.format(
					"readerIndex(%d) + length(%d) exceeds writerIndex(%d): %s",
					readerIndex(), minimumReadableBytes, writerIndex(), this));
		}
	}

	@Override
	public void readBytes(ChannelBuffer dst, int dstIndex, int length) {
		checkReadableBytes(length);
		getBytes(readerIndex(), dst, dstIndex, length);
		readerIndex(readerIndex() + length);
	}

	@Override
	public ChannelBuffer readBytes(int length) {
		return new Netty4ChannelBuffer(buf.readBytes(length));
	}

	@Override
	public void resetReaderIndex() {
		buf.resetReaderIndex();
	}

	@Override
	public void resetWriterIndex() {
		buf.resetWriterIndex();
	}

	@Override
	public int readerIndex() {
		return buf.readerIndex();
	}

	@Override
	public void readerIndex(int readerIndex) {
		buf.readerIndex(readerIndex);
	}

	@Override
	public void readBytes(OutputStream dst, int length) throws IOException {
		buf.readBytes(dst, length);
	}

	@Override
	public void setByte(int index, int value) {
		buf.setByte(index, value);
	}

	@Override
	public void setBytes(int index, byte[] src) {
		buf.setBytes(index, src);
	}

	@Override
	public void setBytes(int index, byte[] src, int srcIndex, int length) {
		buf.setBytes(srcIndex, src, srcIndex, length);
	}

	@Override
	public void setBytes(int index, ByteBuffer src) {
		buf.setBytes(index, src);
	}

	@Override
	public void setBytes(int index, ChannelBuffer src) {
		setBytes(index, src, src.readableBytes());
	}

	@Override
	public void setBytes(int index, ChannelBuffer src, int length) {
		checkIndex(index, length);
		if (src == null) {
			throw new NullPointerException("src");
		}
		if (length > src.readableBytes()) {
			throw new IndexOutOfBoundsException(
					String.format(
							"length(%d) exceeds src.readableBytes(%d) where src is: %s",
							length, src.readableBytes(), src));
		}
		setBytes(index, src, src.readerIndex(), length);
		src.readerIndex(src.readerIndex() + length);
	}

	private void checkIndex(int index, int fieldLength) {
		if (isOutOfBounds(index, fieldLength, capacity())) {
			throw new IndexOutOfBoundsException(String.format(
					"index: %d, length: %d (expected: range(0, %d))", index,
					fieldLength, capacity()));
		}
	}

	private void checkSrcIndex(int index, int length, int srcIndex,
			int srcCapacity) {
		checkIndex(index, length);
		if (isOutOfBounds(srcIndex, length, srcCapacity)) {
			throw new IndexOutOfBoundsException(String.format(
					"srcIndex: %d, length: %d (expected: range(0, %d))",
					srcIndex, length, srcCapacity));
		}
	}

	@Override
	public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
		checkSrcIndex(index, length, srcIndex, src.capacity());
		if (src.hasArray()) {
			buf.setBytes(index, src.array(), srcIndex, length);
		} else {
			byte[] v = new byte[length];
			src.getBytes(srcIndex, v);
			buf.setBytes(index, v);
		}
	}

	@Override
	public int setBytes(int index, InputStream src, int length)
			throws IOException {
		return setBytes(index, src, length);
	}

	@Override
	public void setIndex(int readerIndex, int writerIndex) {
		buf.setIndex(readerIndex, writerIndex);
	}

	@Override
	public void skipBytes(int length) {
		buf.skipBytes(length);
	}

	@Override
	public ByteBuffer toByteBuffer() {
		return buf.nioBuffer();
	}

	@Override
	public ByteBuffer toByteBuffer(int index, int length) {
		return buf.nioBuffer(index, length);
	}

	@Override
	public boolean writable() {
		return buf.isWritable();
	}

	@Override
	public int writableBytes() {
		return buf.writableBytes();
	}

	@Override
	public void writeByte(int value) {
		buf.writeByte(value);
	}

	@Override
	public void writeBytes(byte[] src) {
		buf.writeBytes(src);
	}

	@Override
	public void writeBytes(byte[] src, int index, int length) {
		buf.writeBytes(src, index, length);
	}

	@Override
	public void writeBytes(ByteBuffer src) {
		buf.writeBytes(src);
	}

	@Override
	public void writeBytes(ChannelBuffer src) {
		writeBytes(src, src.readableBytes());
	}

	@Override
	public void writeBytes(ChannelBuffer src, int length) {
		if (length > src.readableBytes()) {
			throw new IndexOutOfBoundsException(
					String.format(
							"length(%d) exceeds src.readableBytes(%d) where src is: %s",
							length, src.readableBytes(), src));
		}
		writeBytes(src, src.readerIndex(), length);
		src.readerIndex(src.readerIndex() + length);
	}

	private void ensureWritable(int minWritableBytes) {
		if (minWritableBytes < 0) {
			throw new IllegalArgumentException(String.format(
					"minWritableBytes: %d (expected: >= 0)", minWritableBytes));
		}
		ensureWritable0(minWritableBytes);
	}

	private void ensureWritable0(int minWritableBytes) {
		if (minWritableBytes <= writableBytes()) {
			return;
		}

		if (minWritableBytes > buf.maxCapacity() - writerIndex()) {
			throw new IndexOutOfBoundsException(
					String.format(
							"writerIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d): %s",
							writerIndex(), minWritableBytes, buf.maxCapacity(),
							this));
		}

		// Normalize the current capacity to the power of 2.
		int newCapacity = buf.alloc().calculateNewCapacity(
				buf.writerIndex() + minWritableBytes, buf.maxCapacity());

		// Adjust to the new capacity.
		buf.capacity(newCapacity);
	}

	@Override
	public void writeBytes(ChannelBuffer src, int srcIndex, int length) {
		ensureWritable(length);
		if (src.hasArray()) {
			buf.writeBytes(src.array(), srcIndex, length);
		} else {
			byte[] v = new byte[length];
			src.getBytes(srcIndex, v);
			buf.writeBytes(v);
		}
	}

	@Override
	public int writeBytes(InputStream src, int length) throws IOException {
		return buf.writeBytes(src, length);
	}

	@Override
	public int writerIndex() {
		return buf.writerIndex();
	}

	@Override
	public void writerIndex(int writerIndex) {
		buf.writerIndex(writerIndex);
	}

	@Override
	public byte[] array() {
		return buf.array();
	}

	@Override
	public boolean hasArray() {
		return buf.hasArray();
	}

	@Override
	public int arrayOffset() {
		return buf.arrayOffset();
	}

}
