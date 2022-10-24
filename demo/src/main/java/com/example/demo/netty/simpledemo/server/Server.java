package com.example.demo.netty.simpledemo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


public class Server {

   public void openServer(int port){
       ServerBootstrap bootstrap = new ServerBootstrap();
       EventLoopGroup boss = new NioEventLoopGroup(1);  //create boss group, threadpool size is 1
       EventLoopGroup work = new NioEventLoopGroup(5); //create work group, threadpool size is 5
       bootstrap.group(boss,work);   //组合netty组件
       //配置handle组件
       bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

           @Override
           protected void initChannel(SocketChannel ch) {
               ch.pipeline().addLast("encoder",new StringEncoder());
               ch.pipeline().addLast("decoder",new StringDecoder());
               ch.pipeline().addLast(new ServerChanelHandle());
           }
       });
       bootstrap.channel(NioServerSocketChannel.class);

       try{
           ChannelFuture channel = bootstrap.bind(port).sync();
           System.out.println(("服务端已启动，绑定端口:" + port));
           channel.channel().closeFuture().sync();

       } catch (InterruptedException e) {
           e.printStackTrace();
       }finally {
           boss.shutdownGracefully();
           work.shutdownGracefully();
       }


   }

   public static void main(String[] args){
      Server server = new Server();
      server.openServer(8090);
   }

}

