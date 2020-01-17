package com.shxy.asyncrpc;

import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ReomteCallbackInvocationHandler implements InvocationHandler {

    private ChannelHandlerContext mCtx;
    private long mRequestId;

    public ReomteCallbackInvocationHandler(ChannelHandlerContext ctx, long requestId) {
        mCtx = ctx;
        mRequestId = requestId;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcCallbackPackage rpcCallbackPackage = new RpcCallbackPackage();
        rpcCallbackPackage.setCallbackName(method.getDeclaringClass().getName());
        rpcCallbackPackage.setCallbackMethodParamType(RpcUtils.classArray2StringArray(method.getParameterTypes()));
        rpcCallbackPackage.setCallbackMethodParams(args);
        rpcCallbackPackage.setCallbackMethodName(method.getName());
        rpcCallbackPackage.setRequestId(mRequestId);
        mCtx.writeAndFlush(rpcCallbackPackage);
        return new Object();
    }
}
