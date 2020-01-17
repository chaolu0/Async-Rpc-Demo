package com.shxy.asyncrpc;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;

public class RpcCallPackage implements Serializable {
    //发送：请求号，类名，方法名，参数类型，参数。如果参数类型为interface，传递null
    private long mRequestId;
    private String mClassName;
    private String mMethodName;
    private String[] mParamType;
    private Object[] mParam;

    public long getRequestId() {
        return mRequestId;
    }

    public void setRequestId(long requestId) {
        mRequestId = requestId;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public void setMethodName(String methodName) {
        mMethodName = methodName;
    }

    public String[] getParamType() {
        return mParamType;
    }

    public void setParamType(String[] paramType) {
        mParamType = paramType;
    }

    public Object[] getParam() {
        return mParam;
    }

    public void setParam(Object[] param) {
        mParam = param;
    }
}
