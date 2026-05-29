package com.rickstaff.app.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private boolean success;
    private String token;

    @SerializedName("usuario")
    private UserInfo user;
    private String message;

    public boolean isSuccess() { return success; }
    public String getToken() { return token; }
    public UserInfo getUser() { return user; }
    public String getMessage() { return message; }

    public static class UserInfo {
        private int id;
        private String nome;
        private String email;

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }
    }
}