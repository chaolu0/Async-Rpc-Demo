package com.shxy.asyncrpc.test;

import java.lang.reflect.*;

public class InvocationTest {

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> proxyClass = Proxy.getProxyClass(A.class.getClassLoader(), A.class, B.class);
        Constructor<?> constructor = proxyClass.getConstructor(InvocationHandler.class);
        InvocationHandler mInvocationHandler = new MyInvocationHandler();
        Object proxy = constructor.newInstance(mInvocationHandler);
        for (Class clazz :proxy.getClass().getInterfaces()) {
            System.out.println(clazz.getName());
        }
        Method method = A.class.getDeclaredMethod("f");
        System.out.println(method.getDeclaringClass().getName());
    }

    public interface A{
        void f();
    }

    public interface B{
        void g();
        void h();
    }

    public static class MyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }
}
