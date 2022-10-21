package com.example.demo.java.reactor.server;

import com.example.demo.java.reactor.ExecuteService;
import com.example.demo.java.reactor.util.ThreadPoolUtil;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


/**
 * Acceptor
 *
 */
public class Acceptor implements ExecuteService {

	private final Selector selector;

	private final ServerSocketChannel serverSocketChannel;

	public ServerSocketChannel getServerSocketChannel() {
		return serverSocketChannel;
	}

	Acceptor(Reactor reactor) {
		this.serverSocketChannel = reactor.serverSocketChannel;
		this.selector = reactor.selector;
	}

	/**
	 * 提交任务初始化
	 */
	public void build(){
		ThreadPoolUtil.execute(() -> {
			SocketChannel socketChannel;
			try {
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				socketChannel = serverSocketChannel.accept();
				if (socketChannel != null) {
					System.out.printf("已接受客户端: %s%n", socketChannel.getRemoteAddress());

					// 这里把客户端通道传给Handler，Handler负责接下来的事件处理
					new AsyncHandler(socketChannel, selector);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
