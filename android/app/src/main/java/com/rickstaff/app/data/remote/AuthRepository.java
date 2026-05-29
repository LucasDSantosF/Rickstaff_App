package com.rickstaff.app.data.remote;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.rickstaff.app.data.model.LoginResponse;

import java.util.HashMap;
import java.util.Map;

import okhttp3.*;
import java.io.IOException;

public class AuthRepository {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Gson gson = new Gson();
    private final OkHttpClient client;
    private final String baseUrl;

    public AuthRepository() {
        this.client = ApiClient.getClient();
        this.baseUrl = ApiClient.BASE_URL;
    }

    public AuthRepository(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    public interface AuthCallback {
        void onSuccess(LoginResponse response);
        void onError(String message);
    }

    public void login(String email, String senha, AuthCallback callback) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("senha", senha);

        RequestBody body = RequestBody.create(gson.toJson(payload), JSON);

        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Erro de conexão: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseBody = response.body().string();
                LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);

                if (response.isSuccessful() && loginResponse.isSuccess()) {
                    callback.onSuccess(loginResponse);
                } else {
                    String msg = loginResponse.getMessage() != null
                            ? loginResponse.getMessage() : "Credenciais inválidas";
                    callback.onError(msg);
                }
            }
        });
    }

    public void register(String nome, String email, String senha, AuthCallback callback) {
        Map<String, String> payload = new HashMap<>();
        payload.put("nome", nome);
        payload.put("email", email);
        payload.put("senha", senha);

        RequestBody body = RequestBody.create(gson.toJson(payload), JSON);

        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/register")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Erro de conexão: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseBody = response.body().string();
                LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);

                if (response.isSuccessful() && loginResponse.isSuccess()) {
                    callback.onSuccess(loginResponse);
                } else {
                    String msg = loginResponse.getMessage() != null
                            ? loginResponse.getMessage() : "Erro ao criar usuário.";
                    callback.onError(msg);
                }
            }
        });
    }
}