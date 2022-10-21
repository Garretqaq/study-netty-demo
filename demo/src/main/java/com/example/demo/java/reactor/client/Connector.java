package com.example.demo.java.reactor.client;

import com.example.demo.java.reactor.ExecuteService;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Connector implements ExecuteService {

	private final Selector selector;

	private final SocketChannel socketChannel;

	Connector(SocketChannel socketChannel, Selector selector) {
		this.socketChannel = socketChannel;
		this.selector = selector;
	}

	@Override
	public void build() {
		try {
			if (socketChannel.finishConnect()) {
				// 这里连接完成（与服务端的三次握手完成）
				System.out.printf("connected to %s%n", socketChannel.getRemoteAddress());

				// 连接建立完成后，接下来的动作交给Handler去处理（读写等）
				new Handler(socketChannel, selector);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
