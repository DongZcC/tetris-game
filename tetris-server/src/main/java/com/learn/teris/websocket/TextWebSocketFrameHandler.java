package com.learn.teris.websocket;

import com.learn.teris.http.HttpRequestHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.HashMap;

/**
 * 功能说明: <br>
 * 系统版本: v1.0<br>
 * 开发人员: @author dongzc15247<br>
 * 开发时间: 2018-03-01<br>
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup group;
    private HashMap<Channel, String> channelName = new HashMap<>();
    private int count;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }


    /**
     * 重写eventTriggered 方法， 处理自定义事件
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // super.userEventTriggered(ctx, evt);
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            // 如果是握手升级连接， 删除掉原本的Http处理器
            ctx.pipeline().remove(HttpRequestHandler.class);
            // 通知所有连接的WebSocket客户端有新的客户端连接上
            //group.writeAndFlush(new TextWebSocketFrame("Client " + ctx.channel() + "joined"));
            group.add(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 增加消息的引用计数, 并将它写到ChannelGroup中的所有已连接客户端
        group.writeAndFlush(msg.retain());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        count++;
        Channel channel = ctx.channel();
        group.add(channel);
        channelName.put(channel, "client" + count);
        group.writeAndFlush(new TextWebSocketFrame(channelName.get(channel) + " 加入聊天"));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        group.writeAndFlush(new TextWebSocketFrame(channelName.get(channel) + " 离开聊天"));
    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
//        Channel channel = ctx.channel();
//        group.writeAndFlush(new TextWebSocketFrame("Client " + channel.remoteAddress() + " 在线"));
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
//        Channel channel = ctx.channel();
//        group.writeAndFlush(new TextWebSocketFrame("Client " + channel.remoteAddress() + " 掉线"));
//    }

}
