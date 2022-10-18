/**
 * Welcome to https://waylau.com
 */
package com.example.demo.java.AIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Async Echo Server.
 * 
 * @since 1.0.0 2019年9月29日
 * @author <a href="https://waylau.com">Way Lau</a>
 */
public class AioSyncServer {
	public static int DEFAULT_PORT = 7;

	/**
	 */
	public static void main(String[] args) {
		AsynchronousServerSocketChannel serverChannel;
		try {
			serverChannel = AsynchronousServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(DEFAULT_PORT);
			serverChannel.bind(address);

			// 设置阐述
			serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
			serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

			System.out.println("AsyncEchoServer已启动，端口：" + DEFAULT_PORT);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}

		while (true) {

			// 可连接
			Future<AsynchronousSocketChannel> future = serverChannel.accept();
			AsynchronousSocketChannel socketChannel = null;
			try {
				socketChannel = future.get();
			} catch (InterruptedException | ExecutionException e) {
				System.out.println("AsyncEchoServer异常!" + e.getMessage());
			}

			System.out.println("AsyncEchoServer接受客户端的连接：" + socketChannel);

			// 分配缓存区
			ByteBuffer buffer = ByteBuffer.allocate(100);

			try {
				while (socketChannel != null && socketChannel.read(buffer).get() != -1) {
					// 当从Channel读取到数据切换到读模式
					buffer.flip();
					Charset gbk = Charset.forName("gbk");
					// 转换用户消息
					String userMessage = new String(buffer.array(), buffer.position(), buffer.limit(), gbk);
					System.out.println("客户端发送消息  -> "
							+ socketChannel.getRemoteAddress() + "：" + userMessage);
					
					if (buffer.hasRemaining()) {
						buffer.compact();
					} else {
						buffer.clear();
					}
				}

				if (socketChannel != null && socketChannel.isOpen()){
					socketChannel.close();
				}
			} catch (InterruptedException | ExecutionException | IOException e) {
				System.out.println("Server异常!" + e.getMessage());
			}

		}

	}
}
