package com.shxy.asyncrpc;

public interface ArithmeticService {

    int add(Integer a, Integer b);

    void divide(Integer a, Integer b, @RemoteCallback OnResult result, @RemoteCallback OnError onError);
}
