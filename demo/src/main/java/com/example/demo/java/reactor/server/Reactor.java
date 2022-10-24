package com.example.demo.java.reactor.server;

import com.example.demo.java.reactor.ExecuteService;
import com.example.demo.java.reactor.util.ThreadPoolUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 上面那种模式只能保证单客户端，和单服务器的通信方式，如果多客户端通信服务器是实现不了的
 * 反应器模式
 * 用于解决多用户访问并发问题
 * 
 * 举个例子：餐厅服务问题
 * 
 * 传统线程池做法：来一个客人(请求)去一个服务员(线程)
 * 反应器模式做法：当客人点菜的时候，服务员就可以去招呼其他客人了，等客人点好了菜，直接招呼一声“服务员”
 * 
 * @author linxcool
 */
public class Reactor{
	public final Selector selector;
	public final ServerSocketChannel serverSocketChannel;

	public Reactor(int port) throws IOException {
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
		serverSocketChannel.socket().bind(inetSocketAddress);
		serverSocketChannel.configureBlocking(false);
		System.out.println("服务端已启动---端口:  "+ port);
		//向selector注册该channel  
		SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		//利用selectionKey的attache功能绑定Acceptor 如果有事情，触发Acceptor 
		selectionKey.attach(new Acceptor(this));
	}

	public void build(){
		ThreadPoolUtil.execute(() -> {
			try {
				while (true) {
					// 若没有事件则开始进行阻塞
					selector.select();
					Set<SelectionKey> selectionKeys = selector.selectedKeys();
					Iterator<SelectionKey> it = selectionKeys.iterator();
					//Selector如果发现channel有OP_ACCEPT或READ事件发生，下列遍历就会进行。
					while (it.hasNext()) {
						//来一个事件 第一次触发一个accepter线程
						SelectionKey selectionKey = it.next();

						// 从key中获取之前绑定执行对象
						Object attachment = selectionKey.attachment();

						/*
						  如果产生连接事件,此时attachment为Acceptor
						  当新连接事件产生，select由阻塞转变为非阻塞，将连接事件交给Acceptor去处理。
						  Acceptor用传过来的severChannel处理连接事件，本应selector应转为阻塞，事件已处理.
						  但同时Acceptor将读写事件交给AsyncHandler处理，重新注册了读写事件的key
						  此时attachment为AsyncHandler
						  selector便不再阻塞，直至读写事件的key给cancel(),才会变为阻塞
						 */
						ExecuteService acceptor = (ExecuteService) attachment;
						acceptor.build();
						// 清除监听事件产生的keys
						it.remove();
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

}