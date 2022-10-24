package com.example.demo.netty.simpledemo.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;


public class ServerChanelHandle extends SimpleChannelInboundHandler<String> {

    //必须定义为类成员变量。每个客户端连接时，都会new ChatServerHandler。static保证数据共享
    public static ChannelGroup cg = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();
        for(Channel chanel: cg){
            chanel.writeAndFlush(ch.remoteAddress()+"进来啦！");
        }
        cg.add(ch);
    }

    /**
     * 上线处理
     * @param ctx context
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        for(Channel ch: cg){
          ch.writeAndFlush(channel.remoteAddress()+"上线啦");
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        cg.remove(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String customerAddress = channel.remoteAddress().toString();
        for(Channel ch:cg){
            ch.writeAndFlush("客户端" + customerAddress + "下线了！");
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        for(Channel ch: cg){
            if(channel == ch){
                ch.writeAndFlush("我说"+msg);
            }else {
                ch.writeAndFlush(channel.remoteAddress()+"说："+msg);
            }
        }
    }
}

