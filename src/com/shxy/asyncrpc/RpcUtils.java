package com.shxy.asyncrpc;

import io.netty.channel.ChannelHandlerContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;

public class RpcUtils {

    public static Class[] stringArray2ClassArray(String[] strings) throws ClassNotFoundException {
        Class[] classes = new Class[strings.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = Class.forName(strings[i]);
        }
        return classes;
    }

    public static Object[] generateProxyArguments(ChannelHandlerContext ctx, RpcCallPackage rpcCallPackage) throws ClassNotFoundException, NoSuchMethodException {
        Object[] sourceParams = rpcCallPackage.getParam();
        Class[] paramsType = stringArray2ClassArray(rpcCallPackage.getParamType());
        // 使用 rpcCallPackage里面的类名，这是原始接口名，带有注解信息
        Annotation[][] annotations = Class.forName(rpcCallPackage.getClassName())
                .getDeclaredMethod(rpcCallPackage.getMethodName(), stringArray2ClassArray(rpcCallPackage.getParamType()))
                .getParameterAnnotations();
        Object[] objects = new Object[sourceParams.length];
        for (int i = 0; i < objects.length; i++) {
            boolean isRemoteCall = false;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof RemoteCallback) {
                    // 代理的回调接口类，类加载器，ctx，requestId
                    objects[i] = Proxy.newProxyInstance(paramsType[i].getClassLoader(),
                            new Class[]{paramsType[i]}, new ReomteCallbackInvocationHandler(ctx, rpcCallPackage.getRequestId()));
                    isRemoteCall = true;
                    break;
                }
            }
            if (!isRemoteCall) {
                objects[i] = sourceParams[i];
            }
        }
        return objects;
    }

    public static String[] classArray2StringArray(Class[] classes) {
        String[] strings = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            strings[i] = classes[i].getName();
        }
        return strings;
    }
}
