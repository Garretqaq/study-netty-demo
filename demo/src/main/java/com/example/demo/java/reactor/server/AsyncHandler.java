package com.example.demo.java.reactor.server;

import com.example.demo.java.reactor.ExecuteService;
import com.example.demo.java.reactor.util.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Async Handler
 * 
 * @since 1.0.0
 * @author sgz
 */
@Slf4j
public class AsyncHandler implements ExecuteService {

	private final Selector selector;

	private final SelectionKey selectionKey;
	private final SocketChannel socketChannel;

	private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
	private final ByteBuffer sendBuffer = ByteBuffer.allocate(2048);

	private final static int READ = 0; // 读取就绪
	private final static int SEND = 1; // 响应就绪
	private final static int PROCESSING = 2; // 处理中

	private final AtomicInteger status = new AtomicInteger(READ); // 所有连接完成后都是从一个读取动作开始的

	private final Lock lock;



	AsyncHandler(SocketChannel socketChannel, Selector selector) throws IOException {
		this.socketChannel = socketChannel; // 接收客户端连接
		this.socketChannel.configureBlocking(false); // 置为非阻塞模式
		selectionKey = socketChannel.register(selector, 0); // 将该客户端注册到selector
		selectionKey.attach(this); // 附加处理对象，当前是Handler对象
		selectionKey.interestOps(SelectionKey.OP_READ); // 连接已完成，接下来就是读取动作
		this.selector = selector;
		this.selector.wakeup();
		lock = new ReentrantLock(true);
	}
	public void build(){
		// 如果状态在未处理，则表示已经有线程在执行不需要再分配任务
		if (status.get() == PROCESSING){
			return;
		}

		ThreadPoolUtil.execute(() -> {
			synchronized (lock) {
				// 如果未加到锁则代表有线程处理中，
				boolean lockStatus = lock.tryLock();
				if (!lockStatus){
					return;
				}
				// 如果一个任务正在异步处理，那么这个execute是直接不触发任何处理的，
				// read和send只负责简单的数据读取和响应，业务处理完全不阻塞这里的处理
				switch (status.get()) {
					case READ:
						status.set(PROCESSING);
						read();
						break;
					case SEND:
						status.set(PROCESSING);
						send();
						break;
					default:
				}
			}
		});

	}

	private void read() {
		if (selectionKey.isValid()) {
			try {
				readBuffer.clear();

				// read方法结束，意味着本次"读就绪"变为"读完毕"，标记着一次就绪事件的结束
				int count = socketChannel.read(readBuffer);
				if (count > 0) {
					// 读入信息后的业务处理
					readBuffer.flip();
					String message = new String(readBuffer.array(), readBuffer.position(), readBuffer.limit(), StandardCharsets.UTF_8);
					System.out.println("客户端"+ socketChannel.getRemoteAddress() + "的消息-----消息内容:" + message);
					readBuffer.compact();
					// selectionKey.interestOps(SelectionKey.OP_WRITE); // 注册写事件
					this.selector.wakeup(); // 唤醒阻塞在select的线程
				} else if (count < 0){
					// 读模式下拿到的值是-1，说明客户端已经断开连接，那么将对应的selectKey从selector里清除，
					// 否则下次还会select到，因为断开连接意味着读就绪不会变成读完毕，也不cancel，
					// 下次select会不停收到该事件。
					// 所以在这种场景下，需要关闭socketChannel并且取消key，最好是退出当前函数。
					// 注意，这个时候服务端要是继续使用该socketChannel进行读操作的话，
					// 就会抛出“远程主机强迫关闭一个现有的连接”的IO异常。
					selectionKey.cancel();
					socketChannel.close();
					System.out.println("read closed");
				}
			} catch (IOException e) {
				System.err.println("处理read业务时发生异常！异常信息：" + e.getMessage());
				selectionKey.cancel();
				try {
					socketChannel.close();
				} catch (IOException e1) {
					System.err.println("处理read业务关闭通道时发生异常！异常信息：" + e.getMessage());
				}
			}finally {
				lock.unlock();
				status.set(SEND);
				selectionKey.interestOps(SelectionKey.OP_READ); // 重新设置为读
			}

		}
	}

	void send() {
		if (selectionKey.isValid()) {
			// 置为执行中
			status.set(PROCESSING);
			try {
				sendBuffer.clear();
				sendBuffer.put(String
						.format("recived %s from %s",  new String(readBuffer.array()),socketChannel.getRemoteAddress())
						.getBytes());
				sendBuffer.flip();

				// write方法结束，意味着本次写就绪变为写完毕，标记着一次事件的结束
				int count = socketChannel.write(sendBuffer);

				if (count < 0) {
					// 同上，write场景下，取到-1，也意味着客户端断开连接
					selectionKey.cancel();
					socketChannel.close();
					System.out.println("send close");
				}

				// 没断开连接，则再次切换到读
				status.set(READ);
			} catch (IOException e) {
				System.err.println("异步处理send业务时发生异常！异常信息：" + e.getMessage());
				selectionKey.cancel();
				try {
					socketChannel.close();
				} catch (IOException e1) {
					System.err.println("异步处理send业务关闭通道时发生异常！异常信息：" + e.getMessage());
				}
			}finally {
				lock.unlock();
				selectionKey.interestOps(SelectionKey.OP_READ); // 重新设置为读
			}
		}
	}


}
