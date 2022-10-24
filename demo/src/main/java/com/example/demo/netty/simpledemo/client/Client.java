package com.example.demo.netty.simpledemo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;


public class Client implements Runnable{


    private final String serverIP;
    private final int port;
    public Client(String serverIp, int port){
        this.serverIP = serverIp;
        this.port = port;
    }

    @Override
    public void run() {

        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup work = new NioEventLoopGroup(1);
        bootstrap.group(work);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) {
               ch.pipeline().addLast("encode",new StringEncoder());
               ch.pipeline().addLast("decode",new StringDecoder());
               ch.pipeline().addLast(new ClientChanelHandle());
            }
        });
        bootstrap.channel(NioSocketChannel.class);

        ChannelFuture channelFuture = bootstrap.connect(serverIP,port);
        Channel channel = channelFuture.channel();

        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String sendMsg = scanner.nextLine();
            channel.writeAndFlush(sendMsg);
        }
        work.shutdownGracefully();
    }

    public static void main(String[] args){
       new Thread(new Client("127.0.0.1",8090)).start();
    }
}

