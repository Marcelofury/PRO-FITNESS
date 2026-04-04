package com.example.profitness.network;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}
