package com.shxy.asyncrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RpcCallConfig {
    //缓存：请求号，锁，代理对象，回调对象[]
    private long mRequestId;
    private Object mLock;
    private Object mTarget;
    private Map<String,Object> mCallbacks;
    private Object mResult;

    public RpcCallConfig() {
        mLock = new Object();
        mCallbacks = new HashMap<>();
    }

    public long getRequestId() {
        return mRequestId;
    }

    public void setRequestId(long requestId) {
        mRequestId = requestId;
    }

    public Object getLock() {
        return mLock;
    }

    public Object getTarget() {
        return mTarget;
    }

    public void setTarget(Object target) {
        mTarget = target;
    }

    public Object getCallback(String callbackName) {
        return mCallbacks.get(callbackName);
    }

    public void addCallback(String callbackName, Object callback) {
        mCallbacks.put(callbackName, callback);
    }

    public Object getResult() {
        return mResult;
    }

    public void setResult(Object result) {
        mResult = result;
    }
}
