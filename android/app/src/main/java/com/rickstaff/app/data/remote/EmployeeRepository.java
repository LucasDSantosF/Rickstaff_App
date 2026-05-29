package com.rickstaff.app.data.remote;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rickstaff.app.data.model.Employee;
import okhttp3.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class EmployeeRepository {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Gson gson = new Gson();
    private final OkHttpClient client;
    private final String baseUrl;

    public EmployeeRepository() {
        this.client = ApiClient.getClient();
        this.baseUrl = ApiClient.BASE_URL;
    }

    public EmployeeRepository(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    public interface ListCallback {
        void onSuccess(List<Employee> list);
        void onError(String msg);
    }

    public interface ActionCallback {
        void onSuccess(Employee employee);
        void onError(String msg);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String msg);
    }

    public void getAll(ListCallback callback) {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/funcionarios")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Erro de conexão");
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Type type = new TypeToken<List<Employee>>(){}.getType();
                assert response.body() != null;
                List<Employee> list = gson.fromJson(response.body().string(), type);
                callback.onSuccess(list);
            }
        });
    }

    public void create(Employee emp, ActionCallback callback) {
        sendRequest("POST", baseUrl + "/api/funcionarios", emp, callback);
    }

    public void update(Employee emp, ActionCallback callback) {
        sendRequest("PUT", baseUrl + "/api/funcionarios/" + emp.getId(), emp, callback);
    }

    public void delete(int id, DeleteCallback callback) {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/funcionarios/" + id)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Erro de conexão");
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() == 204) callback.onSuccess();
                else callback.onError("Erro ao excluir");
            }
        });
    }

    private void sendRequest(String method, String url, Employee emp, ActionCallback callback) {
        String json = gson.toJson(emp);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Erro de conexão");
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Employee result = gson.fromJson(response.body().string(), Employee.class);
                    callback.onSuccess(result);
                } else {
                    callback.onError("Erro: " + response.code());
                }
            }
        });
    }
}