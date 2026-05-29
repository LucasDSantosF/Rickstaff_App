package com.rickstaff.app.data.remote;

import static com.rickstaff.app.util.Constants.BASE_RICK_MORTY_URL;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.rickstaff.app.data.model.CharacterResponse;
import okhttp3.*;
import java.io.IOException;
import java.util.Objects;

public class CharacterRepository {

    private final Gson gson = new Gson();
    private final OkHttpClient client;
    private final String baseUrl;

    public CharacterRepository() {
        this.client = ApiClient.getClient();
        this.baseUrl = BASE_RICK_MORTY_URL;
    }

    public CharacterRepository(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    public interface CharacterCallback {
        void onSuccess(CharacterResponse response);
        void onError(String message);
    }

    public void getCharacters(int page, String status, String gender, String species,
                              CharacterCallback callback) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl)).newBuilder()
                .addQueryParameter("page", String.valueOf(page));

        if (status != null && !status.isEmpty())
            urlBuilder.addQueryParameter("status", status);
        if (gender != null && !gender.isEmpty())
            urlBuilder.addQueryParameter("gender", gender);
        if (species != null && !species.isEmpty())
            urlBuilder.addQueryParameter("species", species);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Erro de conexão");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Nenhum resultado encontrado");
                    return;
                }
                assert response.body() != null;
                CharacterResponse result = gson.fromJson(
                        response.body().string(), CharacterResponse.class
                );
                callback.onSuccess(result);
            }
        });
    }
}