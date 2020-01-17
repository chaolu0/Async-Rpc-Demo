package com.shxy.asyncrpc;

import java.io.Serializable;

public class RpcCallbackPackage implements Serializable {
    //请求号，异步/同步，同步结果，异步接口名，异步方法名，异步参数类型，异步参数
    private long mRequestId;
    private Object mResult;
    private String mCallbackName;
    private String mCallbackMethodName;
    private String[] mCallbackMethodParamType;
    private Object[] mCallbackMethodParams;

    public long getRequestId() {
        return mRequestId;
    }

    public void setRequestId(long requestId) {
        mRequestId = requestId;
    }

    public Object getResult() {
        return mResult;
    }

    public void setResult(Object result) {
        mResult = result;
    }

    public String getCallbackName() {
        return mCallbackName;
    }

    public void setCallbackName(String callbackName) {
        mCallbackName = callbackName;
    }

    public String getCallbackMethodName() {
        return mCallbackMethodName;
    }

    public void setCallbackMethodName(String callbackMethodName) {
        mCallbackMethodName = callbackMethodName;
    }

    public String[] getCallbackMethodParamType() {
        return mCallbackMethodParamType;
    }

    public void setCallbackMethodParamType(String[] callbackMethodParamType) {
        mCallbackMethodParamType = callbackMethodParamType;
    }

    public Object[] getCallbackMethodParams() {
        return mCallbackMethodParams;
    }

    public void setCallbackMethodParams(Object[] callbackMethodParams) {
        mCallbackMethodParams = callbackMethodParams;
    }
}
