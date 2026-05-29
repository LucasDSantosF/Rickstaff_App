package com.rickstaff.app.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // Emulador: 10.0.2.2 acessa o localhost da sua máquina
    // Dispositivo físico: troque pelo IP da sua máquina na rede Wi-Fi
    public static final String BASE_URL = "http://10.0.2.2:8080";

    private static OkHttpClient client;

    public static OkHttpClient getClient() {
        if (client == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }
}
