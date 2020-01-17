package com.shxy.asyncrpc;

public class IncreaseServiceImpl implements IncreaseService{
    @Override
    public int increase(Integer a) {
        return ++a;
    }

    @Override
    public void increase(Integer a, OnIncreaseResult increaseResult) {
        a++;
        increaseResult.onIncrease(a);
    }
}
