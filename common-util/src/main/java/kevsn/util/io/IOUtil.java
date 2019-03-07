/*
 * @(#) IOUtil.java 2014-9-6	 
 *	
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;

import com.vamsc.util.exception.RuntimeIoException;
import com.vamsc.util.exception.VAMSCException;

import kevsn.util.ArrayUtils;
import kevsn.util.fns.ExceptionalBiConsumer;
import kevsn.util.fns.ExceptionalConsumer;
import kevsn.util.fns.ExceptionalSupplier;

/**
 * 所有的{@link IOException}都已经转化为{@link RuntimeIoException}抛出
 * 
 * @author Kevin
 * 
 */
public class IOUtil {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;// 4K

	public static final String LOCK_NAME = "file.lock";

	private IOUtil() {

	}

	public static void writeTextContent(String stringContent, Path file) {
		try {
			_writeTextContent(stringContent, file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	public static void writeTextContent(String stringContent, Path file,
			Charset charset) throws RuntimeIoException {
		try {
			_writeTextContent(stringContent, file, charset);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static void _writeTextContent(String stringContent, Path file,
			Charset charset) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
			writer.write(stringContent);
		}
	}

	/**
	 * 读取reader对应的所有内容
	 * <p>
	 * <strong>不要读取大文件</strong>
	 * 
	 * 如果要分行读取，可以用{@link IOUtils#readLines(Reader)}
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String readTextContent(Reader reader)
			throws RuntimeIoException {
		try {
			return _readTextContent(reader);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	public static String readTextContent(URL url, Charset charset) {
		String content;
		try (InputStream stream = url.openStream();
				InputStreamReader reader = new InputStreamReader(stream,
						charset)) {
			content = IOUtil.readTextContent(reader);
			return content;
		} catch (IOException e) {
			throw new VAMSCException(e);
		}
	}

	private static String _readTextContent(Reader reader)
			throws RuntimeIoException, IOException {
		StringBuilder builder = new StringBuilder(2000);
		char[] chars = new char[1000];
		int readed;
		while ((readed = reader.read(chars)) != -1) {
			builder.append(chars, 0, readed);
		}
		chars = null;
		return builder.toString();
	}

	/**
	 * @see #readTextContent(Reader)
	 * @param file
	 * @return
	 */
	public static String readTextContent(Path file) throws RuntimeIoException {
		try (BufferedReader reader = Files.newBufferedReader(file,
				StandardCharsets.UTF_8)) {
			return _readTextContent(reader);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	public static String readTextContent(Path file, Charset charset)
			throws RuntimeIoException {
		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
			return _readTextContent(reader);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	public static <T extends Throwable> long readFile(Path file,
			long totalReadLength,
			ExceptionalBiConsumer<byte[], Integer, T> consumer) throws T {
		try {
			return _readFile(file, totalReadLength, consumer);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static <T extends Throwable> long _readFile(Path file,
			long totalReadLength,
			ExceptionalBiConsumer<byte[], Integer, T> consumer)
			throws IOException, T {
		try (InputStream stream = Files.newInputStream(file)) {
			return readStream(stream, totalReadLength, consumer);
		}
	}

	public static <T extends Throwable> long readFile(Path file,
			ExceptionalBiConsumer<byte[], Integer, T> consumer) throws T {
		try {
			return _readFile(file, consumer);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static <T extends Throwable> long _readFile(Path file,
			ExceptionalBiConsumer<byte[], Integer, T> consumer)
			throws IOException, T {
		try (InputStream stream = Files.newInputStream(file)) {
			return readStream(stream, consumer);
		}
	}

	public static <T extends Throwable> long readStream(InputStream input,
			long totalReadLength,
			ExceptionalBiConsumer<byte[], Integer, T> consumer) throws T {
		try {
			return _readStream(input, totalReadLength, consumer);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static <T extends Throwable> long _readStream(InputStream input,
			long totalReadLength,
			ExceptionalBiConsumer<byte[], Integer, T> consumer)
			throws IOException, T {
		int blen = totalReadLength > DEFAULT_BUFFER_SIZE ? DEFAULT_BUFFER_SIZE
				: (int) totalReadLength;
		byte[] bytes = new byte[blen];
		long totalRead = 0;
		while (totalReadLength > totalRead) {
			int loopTryRead = blen;
			long sub = totalReadLength - totalRead;
			if (loopTryRead > sub) {
				loopTryRead = (int) sub;
			}
			int loopRealRead = input.read(bytes, 0, loopTryRead);
			if (loopRealRead < 0) {
				break;
			}
			consumer.accept(bytes, loopRealRead);
			totalRead += loopRealRead;
		}
		return totalRead;
	}

	public static <T extends Throwable> long readStream(InputStream input,
			ExceptionalBiConsumer<byte[], Integer, T> consumer) throws T {
		try {
			return _readStream(input, consumer);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static <T extends Throwable> long _readStream(InputStream input,
			ExceptionalBiConsumer<byte[], Integer, T> consumer)
			throws IOException, T {
		byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
		int l;
		long totalRead = 0;
		while ((l = input.read(bytes)) != -1) {
			consumer.accept(bytes, l);
			totalRead += l;
		}
		return totalRead;
	}

	public static <T extends Throwable> long bufferReadChannel(
			ReadableByteChannel channel, boolean directBuffer,
			ExceptionalConsumer<ByteBuffer, T> consumer) throws T {
		try {
			return _readChannel(channel, directBuffer, DEFAULT_BUFFER_SIZE,
					consumer);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	public static <E extends Throwable> long bufferReadChannel(
			ReadableByteChannel channel, boolean directBuffer, int bufferSize,
			ExceptionalConsumer<ByteBuffer, E> consumer) throws E {
		try {
			return _readChannel(channel, directBuffer, bufferSize, consumer);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static <T extends Throwable> long _readChannel(
			ReadableByteChannel channel, boolean directBuffer, int bufferSize,
			ExceptionalConsumer<ByteBuffer, T> consumer) throws IOException, T {
		ByteBuffer buffer = directBuffer ? ByteBuffer.allocateDirect(bufferSize)
				: ByteBuffer.allocate(bufferSize);
		long totalRead = 0;
		int l;
		while ((l = channel.read(buffer)) != -1) {
			buffer.flip();
			consumer.accept(buffer);
			buffer.clear();
			totalRead += l;
		}
		return totalRead;
	}

	public static <T extends Throwable> long readChannelWithTotalLength(
			ReadableByteChannel channel, boolean directBuffer,
			long totalReadLength, ExceptionalConsumer<ByteBuffer, T> consumer)
			throws T {
		try {
			return _readChannel(channel, directBuffer, totalReadLength,
					DEFAULT_BUFFER_SIZE, consumer);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	public static <E extends Throwable> long readChannelWithTotalLength(
			ReadableByteChannel channel, boolean directBuffer,
			long totalReadLength, int bufferSize,
			ExceptionalConsumer<ByteBuffer, E> consumer) throws E {
		try {
			return _readChannel(channel, directBuffer, totalReadLength,
					bufferSize, consumer);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static <T extends Throwable> long _readChannel(
			ReadableByteChannel channel, boolean directBuffer,
			long totalReadLength, int bufferSize,
			ExceptionalConsumer<ByteBuffer, T> consumer) throws IOException, T {
		int blen = totalReadLength > bufferSize ? bufferSize
				: (int) totalReadLength;
		long totalRead = 0;
		ByteBuffer buffer = directBuffer ? ByteBuffer.allocateDirect(blen)
				: ByteBuffer.allocate(blen);
		while (totalReadLength > totalRead) {
			int loopTryRead = blen;
			long sub = totalReadLength - totalRead;
			if (loopTryRead > sub) {
				loopTryRead = (int) sub;
			}
			buffer.limit(loopTryRead);
			int loopRealRead = channel.read(buffer);
			if (loopRealRead < 0) {
				break;
			}
			buffer.flip();
			consumer.accept(buffer);
			totalRead += loopRealRead;
			buffer.clear();
		}
		return totalRead;
	}

	public static boolean isEqualFiles(Path... paths) {
		if (paths.length <= 1) {
			return true;
		}
		InputStream[] streams;
		try {
			streams = ArrayUtils.mapArrayExceptional(InputStream.class, paths,
					p -> Files.newInputStream(p));
			return isEqualStreams(streams);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	public static boolean isEqualStreams(InputStream... streams) {
		try {
			return _isEqualStreams(streams);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static boolean _isEqualStreams(InputStream... streams)
			throws IOException {
		if (streams.length <= 1) {
			return true;
		}
		byte[] bytes1 = new byte[DEFAULT_BUFFER_SIZE];
		byte[] bytes2 = new byte[DEFAULT_BUFFER_SIZE];
		while (true) {
			int read1 = streams[0].read(bytes1);
			int read2 = 0;
			for (int i = 1; i < streams.length; i++) {
				InputStream stream = streams[i];
				read2 = stream.read(bytes2);
				if (read1 != read2
						|| !isEqualByteArray(bytes1, read1, bytes2, read2)) {
					return false;
				}
				read1 = read2;
				byte[] temp = bytes1;
				bytes1 = bytes2;
				bytes2 = temp;
			}
			if (read2 < 0) {
				return true;
			}
		}
	}

	public static boolean isEqualChannels(ReadableByteChannel... channels) {
		try {
			return _isEqualChannels(channels);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private static boolean _isEqualChannels(ReadableByteChannel... channels)
			throws IOException {
		if (channels.length <= 1) {
			return true;
		}
		ByteBuffer buffer1 = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		ByteBuffer buffer2 = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		while (true) {
			int read1 = channels[0].read(buffer1);
			buffer1.flip();
			int read2 = 0;
			for (int i = 1; i < channels.length; i++) {
				ReadableByteChannel channel = channels[i];
				read2 = channel.read(buffer2);
				buffer2.flip();
				if (read1 != read2 || !buffer1.equals(buffer2)) {
					return false;
				}
				read1 = read2;
				ByteBuffer temp = buffer1;
				buffer1 = buffer2;
				buffer2 = temp;
				buffer2.clear();
			}
			if (read2 < 0) {
				return true;
			}
		}
	}

	private static boolean isEqualByteArray(byte[] byte1, int len1,
			byte[] byte2, int len2) {
		if (len1 != len2) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			if (byte1[i] != byte2[i]) {
				return false;
			}
		}
		return true;
	}

	public static void readLines(
			ExceptionalSupplier<Reader, Throwable> readerSupplier,
			Predicate<String> handler) {
		Reader reader;
		try {
			reader = readerSupplier.get();
			if (reader instanceof BufferedReader) {
				_readLines((BufferedReader) reader, handler);
			} else {
				_readLines(new BufferedReader(reader), handler);
			}
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		} catch (Throwable e) {
			throw new VAMSCException(e);
		}
	}

	public static void readLines(Path file, Charset charset,
			Predicate<String> handler) {
		readLines(() -> Files.newBufferedReader(file, charset), handler);
	}

	private static void _readLines(BufferedReader reader,
			Predicate<String> handler) throws IOException {
		try (Reader rd = reader) {
			String line;
			BufferedReader breader = new BufferedReader(rd);
			while ((line = breader.readLine()) != null) {
				boolean ok = handler.test(line);
				if (!ok) {
					break;
				}
			}
		}
	}

}
