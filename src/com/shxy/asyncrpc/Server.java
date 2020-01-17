package com.shxy.asyncrpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.Method;
import java.util.HashMap;

public class Server extends Thread {

    private HashMap<Class, Class> mClassMap = new HashMap<>();

    public static void main(String[] args) {
        Server service = new Server();
        service.registerService(ArithmeticService.class, ArithmeticServiceImpl.class);
        service.registerService(IncreaseService.class, IncreaseServiceImpl.class);
        service.start();
    }

    public boolean registerService(Class interfaceClass, Class clazz) {
        if (mClassMap.containsKey(interfaceClass)) {
            return false;
        }
        Class put = mClassMap.put(interfaceClass, clazz);
        return true;
    }

    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup masterGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(masterGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(new ClassResolver() {
                    @Override
                    public Class<?> resolve(String className) throws ClassNotFoundException {
                        return Class.forName(className);
                    }
                }), new ServerRpcHandler(mClassMap));
            }
        });
        bootstrap.option(ChannelOption.SO_BACKLOG, 128);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        System.out.println("server starting...");
        ChannelFuture bindFuture = bootstrap.bind(8880);
        try {
            bindFuture.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("server started");
    }

    private static class ServerRpcHandler extends ChannelInboundHandlerAdapter {

        public ServerRpcHandler(HashMap<Class, Class> mClassMap) {
            this.mClassMap = mClassMap;
        }

        private HashMap<Class, Class> mClassMap = new HashMap<>();

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            System.out.println("a new connect, the connect id is :" + ctx.channel().id());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            RpcCallPackage rpcCallPackage = (RpcCallPackage) msg;
            Class[] paramsType = RpcUtils.stringArray2ClassArray(rpcCallPackage.getParamType());
            Class invokeClass = mClassMap.get(Class.forName(rpcCallPackage.getClassName()));
            Method method = invokeClass.getDeclaredMethod(rpcCallPackage.getMethodName(),
                    paramsType);
            // 异步方法，在RemoteCallback代理中返回结果
            Object result = method.invoke(invokeClass.newInstance(), RpcUtils.generateProxyArguments(ctx, rpcCallPackage));
            // 如果没有返回值，认为是同步方法
            if (!method.getReturnType().equals(void.class)) {
                RpcCallbackPackage rpcCallbackPackage = new RpcCallbackPackage();
                rpcCallbackPackage.setRequestId(rpcCallPackage.getRequestId());
                rpcCallbackPackage.setResult(result);
                ctx.writeAndFlush(rpcCallbackPackage);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            cause.printStackTrace();
        }
    }
}
