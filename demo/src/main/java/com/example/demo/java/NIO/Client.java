/**
 * Welcome to https://waylau.com
 */
package com.example.demo.java.NIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * No Blocking Client.
 * 
 * @since 1.0.0
 * @author sgz
 */
public class Client {
	private static final int DEFAULT_PORT = 7;

	public static void main(String[] args) throws UnknownHostException {
		InetAddress localHost = InetAddress.getLocalHost();
		// 获取本地主机信息
		InetAddress address = InetAddress.getByAddress(localHost.getAddress());
		String hostName = localHost.getHostName();
		System.out.println("本地地址是----" + address);
		System.out.println("本地名称是----" + hostName);

		SocketChannel socketChannel = null;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.connect(new InetSocketAddress(hostName, DEFAULT_PORT));
		} catch (IOException e) {
			System.err.println("NoBlockingClient异常： " + e.getMessage());
			System.exit(1);
		}

		ByteBuffer writeBuffer = ByteBuffer.allocate(32);
		ByteBuffer readBuffer = ByteBuffer.allocate(32);

		try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
			String userInput;
			while ((userInput = stdIn.readLine()) != null) {
				writeBuffer.put(userInput.getBytes());
				writeBuffer.flip();

				// 写消息到管道
				socketChannel.write(writeBuffer);

				// 清理缓冲区
				writeBuffer.clear();
				readBuffer.clear();
				System.out.println("客户端输出: " + userInput);
			}
		} catch (UnknownHostException e) {
			System.err.println("不明主机，主机名为： " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("不能从主机中获取I/O，主机名为：" + hostName);
			System.exit(1);
		}
	}

}
