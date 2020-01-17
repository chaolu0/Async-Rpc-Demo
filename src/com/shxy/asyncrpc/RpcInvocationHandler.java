package com.shxy.asyncrpc;

import io.netty.channel.ChannelHandlerContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RpcInvocationHandler implements InvocationHandler {

    private Client mClient;
    private ChannelHandlerContext mCtx;

    public RpcInvocationHandler(Client client) {
        mClient = client;
        mCtx = client.getCtx();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        long requestId = mClient.generateRequestId();

        RpcCallConfig rpcCallConfig = new RpcCallConfig();
        rpcCallConfig.setRequestId(requestId);
        rpcCallConfig.setTarget(this);

        int paramCount = method.getParameterCount();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] paramTypes = new String[paramCount];
        for(int i = 0 ;i < paramCount; i++) {
            for (Annotation annotation: parameterAnnotations[i]) {
                if (annotation instanceof RemoteCallback) {
                    rpcCallConfig.addCallback(parameterTypes[i].getName(), args[i]);
                    args[i] = "";
                    break;
                }
            }
            paramTypes[i] = parameterTypes[i].getName();
        }

        RpcCallPackage rpcCallPackage = new RpcCallPackage();
        rpcCallPackage.setRequestId(requestId);
        rpcCallPackage.setClassName(method.getDeclaringClass().getName());
        rpcCallPackage.setMethodName(method.getName());
        rpcCallPackage.setParamType(paramTypes);
        rpcCallPackage.setParam(args);
        mClient.addRpcCallConfig(requestId, rpcCallConfig);
        mCtx.writeAndFlush(rpcCallPackage);

        if (method.getReturnType().equals(void.class)) {
            return new Object();
        } else {
            synchronized (rpcCallConfig.getLock()) {
                rpcCallConfig.getLock().wait();
            }
            return rpcCallConfig.getResult();
        }
    }

}
