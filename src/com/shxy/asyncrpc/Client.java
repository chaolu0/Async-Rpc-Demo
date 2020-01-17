package com.shxy.asyncrpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Client extends Thread{

    private long mRequestId = 0;

    private ChannelHandlerContext mCtx;

    private Map<Long, RpcCallConfig> mRpcCallConfigMap;

    public Client(String name) {
        super(name);
        mRpcCallConfigMap = new HashMap<>();
    }

    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(new ClassResolver() {
                    @Override
                    public Class<?> resolve(String className) throws ClassNotFoundException {
                        return Class.forName(className);
                    }
                }),new ClientRpcHandler());
            }
        });
        bootstrap.connect(new InetSocketAddress("127.0.0.1",8880));
    }

    private class ClientRpcHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            mCtx = ctx;
            System.out.println("connected to server");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            RpcCallbackPackage rpcCallbackPackage = (RpcCallbackPackage) msg;
            RpcCallConfig rpcCallConfig = mRpcCallConfigMap.get(rpcCallbackPackage.getRequestId());
            if (rpcCallConfig == null) {
                throw new IllegalStateException("this call has bean consumed!");
            }
            if (rpcCallbackPackage.getResult() != null) {
                rpcCallConfig.setResult(rpcCallbackPackage.getResult());
                synchronized (rpcCallConfig.getLock()){
                    rpcCallConfig.getLock().notify();
                }
            } else {
                Object instance = rpcCallConfig.getCallback(rpcCallbackPackage.getCallbackName());
                Class callback = Class.forName(rpcCallbackPackage.getCallbackName());
                Method callbackMethod = callback.getDeclaredMethod(rpcCallbackPackage.getCallbackMethodName(),
                        RpcUtils.stringArray2ClassArray(rpcCallbackPackage.getCallbackMethodParamType()));
                callbackMethod.invoke(instance,rpcCallbackPackage.getCallbackMethodParams());
            }
            // 只要有返回值，或者某个接口得到了回调，就认为这次请求得到了响应，不在保留rpc信息
            mRpcCallConfigMap.remove(rpcCallbackPackage.getRequestId());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            cause.printStackTrace();
        }
    }

    public synchronized long generateRequestId() {
        mRequestId++;
        return mRequestId;
    }

    public ChannelHandlerContext getCtx() {
        return mCtx;
    }

    public void addRpcCallConfig(long requestId, RpcCallConfig rpcCallConfig) {
        mRpcCallConfigMap.put(requestId, rpcCallConfig);
    }

    public static void main(String[] args) {
        Client client = new Client("client-thread");
        client.start();
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArithmeticService arithmeticService = (ArithmeticService) Proxy.newProxyInstance(ArithmeticService.class.getClassLoader(),
                new Class[]{ArithmeticService.class, IncreaseService.class}, new RpcInvocationHandler(client));
        System.out.println("sync result :" + arithmeticService.add(1 , 1));
        arithmeticService.divide(4, 0, new OnResult() {
            @Override
            public void onResult(Double result) {
                System.out.println("async result :" + result);
            }
        }, new OnError() {
            @Override
            public void onError(Exception e) {
                System.out.println(e);
            }
        });

        IncreaseService increaseService = (IncreaseService) arithmeticService;
        System.out.println(increaseService.increase(1));
        increaseService.increase(2, new OnIncreaseResult() {
            @Override
            public void onIncrease(Integer a) {
                System.out.println(a);
            }
        });
    }
}
