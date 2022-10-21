package com.example.demo.java.reactor.client;


import com.example.demo.java.reactor.ExecuteService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * NIO Client
 * @author sgz
 * @since 1.0.0
 */
public class Client {

    private final static int PORT = 233;

    private final static String IP = "127.0.0.1";

    public static void main(String[] args) {
        Selector selector;
        try {
            selector = Selector.open(); //打开一个Selector
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false); //设置为非阻塞模式
            socketChannel.connect(new InetSocketAddress(IP, PORT)); //连接服务

            //入口，最初给一个客户端channel注册上去的事件都是连接事件
            SelectionKey sk = socketChannel.register(selector, SelectionKey.OP_CONNECT);
            //附加处理类，第一次初始化放的是连接就绪处理类
            sk.attach(new Connector(socketChannel, selector));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                //就绪事件到达之前，阻塞
                selector.select();

                //拿到本次select获取的就绪事件
                Set<SelectionKey> selected = selector.selectedKeys();
                Iterator<SelectionKey> it = selected.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    ExecuteService attachment = (ExecuteService) key.attachment();
                    // 这里进行任务分发
                    attachment.build();
                    it.remove();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
