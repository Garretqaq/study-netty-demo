/**
 * Welcome to https://waylau.com
 */
package com.example.demo.java.BIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server
 * 
 * @since 1.0.0
 * @author sgz
 */
public class Server {

	private static final int DEFAULT_PORT = 7;

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
		System.out.println("BlockingEchoServer已启动，端口：" + DEFAULT_PORT);

		try(
				// 接受客户端建立链接，生成Socket实例(BIO的特点是连接后，该线程会阻塞等待客户端连接，当连接产生则继续进行下去)
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				// 接收客户端的信息
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
				)
		{
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				// 发送信息给客户端
				out.println("我收到你的消息了");
				System.out.println("客户端发送消息 -> " + clientSocket.getRemoteSocketAddress() + ":" + inputLine);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
