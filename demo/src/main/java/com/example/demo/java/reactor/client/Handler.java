package com.example.demo.java.reactor.client;

import com.example.demo.java.reactor.ExecuteService;
import com.example.demo.java.reactor.util.ThreadPoolUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client 处理器
 * @author sgz
 * @since 1.0.0
 */
public class Handler implements ExecuteService {

	private final SelectionKey selectionKey;
	private final SocketChannel socketChannel;

	private final ByteBuffer readBuffer = ByteBuffer.allocate(2048);
	private final ByteBuffer sendBuffer = ByteBuffer.allocate(1024);

	private final static int READ = 0;
	private final static int SEND = 1;

	private final static int WAIT = 2;

	private final AtomicInteger status = new AtomicInteger(SEND);// 与服务端不同，默认最开始是发送数据

	private Selector selector;


	Handler(SocketChannel socketChannel, Selector selector) throws IOException {
		this.socketChannel = socketChannel; // 接收客户端连接
		this.socketChannel.configureBlocking(false); // 置为非阻塞模式
		selectionKey = socketChannel.register(selector, SelectionKey.OP_WRITE); // 将该客户端注册到selector
		selectionKey.attach(this); // 附加处理对象，当前是Handler对象
		selector.wakeup(); // 唤起select阻塞
	}

	@Override
	public void build() {
		if (status.get() == WAIT){
			return;
		}

		ThreadPoolUtil.execute(() -> {
			try {
				switch (status.get()) {
					case SEND:
						send();
						break;
					case READ:
						read();
						break;
					default:
				}
			} catch (IOException e) {
				// 这里的异常处理是做了汇总，同样的，客户端也面临着正在与服务端进行写/读数据时，
				// 突然因为网络等原因，服务端直接断掉连接，这个时候客户端需要关闭自己并退出程序
				System.err.println("send或read时发生异常！异常信息：" + e.getMessage());
				selectionKey.cancel();
				try {
					socketChannel.close();
				} catch (IOException e2) {
					System.err.println("关闭通道时发生异常！异常信息：" + e2.getMessage());
					e2.printStackTrace();
				}
			}
		});
	}

	void send() throws IOException {
		// 状态设置为等待，表示等待用户操作，不再创建新的线程
		status.set(WAIT);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String userMessage;
		while ((userMessage = bufferedReader.readLine()) != null){
			if (selectionKey.isValid()){

				socketChannel.write(ByteBuffer.wrap(userMessage.getBytes()));
				System.out.println("客户端发送消息：" + userMessage);

				// 则再次切换到读，用以接收服务端的响应
//				status.set(READ);
//				selectionKey.interestOps(SelectionKey.OP_READ);
			}else {
				selectionKey.cancel();
				socketChannel.close();
			}
		}
		System.out.println("send执行完成");
	}

	private void read() throws IOException {
		if (selectionKey.isValid()) {
			readBuffer.clear(); // 切换成buffer的写模式，用于让通道将自己的内容写入到buffer里
			socketChannel.read(readBuffer);
			System.out.printf("Server -> Client: %s%n", new String(readBuffer.array()));

			// 收到服务端的响应后，再继续往服务端发送数据
			status.set(SEND);
			selectionKey.interestOps(SelectionKey.OP_WRITE); // 注册写事件
		}
	}

}