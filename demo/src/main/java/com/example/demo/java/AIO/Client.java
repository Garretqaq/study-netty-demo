
package com.example.demo.java.AIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;

/**
 *  Client.
 *  关于buffer相关介绍可参考<a href="http://www.hanhandato.top/archives/buffer">...</a>
 * @since 1.0.0
 * @author sgz
 */
public class Client {

	private static final int DEFAULT_PORT = 7;
	public static void main(String[] args) throws UnknownHostException {
		InetAddress localHost = InetAddress.getLocalHost();

		AsynchronousSocketChannel socketChannel = null;
		try {
			socketChannel = AsynchronousSocketChannel.open();

			// 获取本地主机信息
			InetAddress address = InetAddress.getByAddress(localHost.getAddress());
			System.out.println("本地地址是----" + address);
			System.out.println("本地名称是----" + localHost.getHostName());

			// 建立socket连接
			socketChannel.connect(new InetSocketAddress(address, DEFAULT_PORT));
		} catch (IOException e) {
			System.err.println("AioSyncClient异常： " + e.getMessage());
			System.exit(1);
		}

		ByteBuffer writeBuffer = ByteBuffer.allocate(32);
		try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
			String userInput;
			while ((userInput = stdIn.readLine()) != null) {
				Charset gbk = Charset.forName("gbk");
				ByteBuffer encodeByte = gbk.encode(userInput);

				writeBuffer.put(encodeByte);
				// limit=position,position=0,mark=-1；将缓冲区状态由存数据变为准备取数据
				writeBuffer.flip();
				// 写消息到管道
				socketChannel.write(writeBuffer);

				// 清理缓冲区
				writeBuffer.clear();
				System.out.println("发送: " + userInput);
			}
		} catch (UnknownHostException e) {
			System.err.println("不明主机，主机名为： " + localHost.getHostName());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("不能从主机中获取I/O，主机名为：" + localHost.getHostName());
			System.exit(1);
		}
	}

}
