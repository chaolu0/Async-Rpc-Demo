package com.shxy.asyncrpc;

public class ArithmeticServiceImpl implements ArithmeticService{
    @Override
    public int add(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public void divide(Integer a, Integer b, OnResult result, OnError onError) {
        if (b == 0){
            onError.onError(new ArithmeticException());
        } else {
            double res = a / b;
            result.onResult(res);
        }
    }
}
