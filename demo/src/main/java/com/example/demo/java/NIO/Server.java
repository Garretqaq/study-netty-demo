/**
 * Welcome to https://waylau.com
 */
package com.example.demo.java.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * No Blocking Server.
 * 
 * @since 1.0.0
 * @author sgz
 */
public class Server {
	private static final int DEFAULT_PORT = 7;
	private static final int TIMEOUT = 3000;


	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocketChannel serverChannel;
		Selector selector;

		serverChannel = ServerSocketChannel.open();
		InetSocketAddress address = new InetSocketAddress(DEFAULT_PORT);
		serverChannel.bind(address);
		serverChannel.configureBlocking(false);
		selector = Selector.open();
		// channel注册到选择器当中，监听接收事件
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("NoBlockingServer已启动，端口：" + DEFAULT_PORT);

		while (true) {
			// 当有事件则可以进行处理，中间可以干其他的事情
			if (selector.select(TIMEOUT) == 0) {
				System.out.println("==");
				continue;
			}
			Thread.sleep(4000);
			System.out.println("当前事件" + selector.select());
			System.out.println("当前key的个数为" + selector.selectedKeys().size());

			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while (iterator.hasNext()){
				SelectionKey key = iterator.next();
				// 可连接
				if (key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					SocketChannel socketChannel = server.accept();

					System.out.println("NoBlockingServer接受客户端的连接：" + socketChannel);

					// 设置为非阻塞
					socketChannel.configureBlocking(false);

					// 客户端注册到Selector, 当触发读写事件则会使selector.select()加1
					SelectionKey clientKey = socketChannel.register(selector,
							SelectionKey.OP_WRITE | SelectionKey.OP_READ);

					// 分配缓存区
					ByteBuffer buffer = ByteBuffer.allocate(100);
					clientKey.attach(buffer);
				}

				// 可读
				if (key.isReadable()) {
					SocketChannel client = (SocketChannel) key.channel();
					ByteBuffer output = (ByteBuffer) key.attachment();
					while (client.read(output) > 0){
						output.flip();
						String clientMessage = StandardCharsets.UTF_8.decode(output).toString();
						System.out.println(client.getRemoteAddress() + " -> NoBlockingServer：" + clientMessage);
						//清除之前的数据（覆盖写入）
						output.clear();
					}

				}

				// 可写
//				if (key.isWritable()) {
//					SocketChannel client = (SocketChannel) key.channel();
//					ByteBuffer output = (ByteBuffer) key.attachment();
//					client.write(output);
//					output.flip();
//					String clientMessage = StandardCharsets.UTF_8.decode(output).toString();
//					System.out.println("NoBlockingServer  -> " + client.getRemoteAddress() + "：" + clientMessage);
//
//					output.compact();
//					key.interestOps(SelectionKey.OP_READ);
//				}
				iterator.remove();
			}

		}
	}
}
