package com.example.profitness.network;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private final TokenStore tokenStore;

    public ApiClient(TokenStore tokenStore) {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.tokenStore = tokenStore;
    }

    public Gson getGson() {
        return gson;
    }

    public void get(String path, boolean authRequired, ApiCallback<String> callback) {
        Request.Builder builder = new Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .get();

        applyAuthHeader(builder, authRequired);
        execute(builder.build(), callback);
    }

    public void post(String path, @Nullable Map<String, Object> body, boolean authRequired, ApiCallback<String> callback) {
        RequestBody requestBody = RequestBody.create(gson.toJson(body), JSON);

        Request.Builder builder = new Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .post(requestBody);

        applyAuthHeader(builder, authRequired);
        execute(builder.build(), callback);
    }

    public void put(String path, @Nullable Map<String, Object> body, boolean authRequired, ApiCallback<String> callback) {
        RequestBody requestBody = RequestBody.create(gson.toJson(body), JSON);

        Request.Builder builder = new Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .put(requestBody);

        applyAuthHeader(builder, authRequired);
        execute(builder.build(), callback);
    }

    private void applyAuthHeader(Request.Builder builder, boolean authRequired) {
        if (!authRequired) {
            return;
        }

        String token = tokenStore.getToken();
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
    }

    private void execute(Request request, ApiCallback<String> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network request failed");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    String raw = responseBody != null ? responseBody.string() : "";

                    if (!response.isSuccessful()) {
                        callback.onError(raw.isEmpty() ? "Request failed with status " + response.code() : raw);
                        return;
                    }

                    callback.onSuccess(raw);
                } catch (IOException e) {
                    callback.onError("Failed to parse server response");
                }
            }
        });
    }
}
