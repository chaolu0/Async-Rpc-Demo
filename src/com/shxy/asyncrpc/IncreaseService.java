package com.shxy.asyncrpc;

public interface IncreaseService {

    int increase(Integer a);

    void increase(Integer a, @RemoteCallback OnIncreaseResult increaseResult);
}
