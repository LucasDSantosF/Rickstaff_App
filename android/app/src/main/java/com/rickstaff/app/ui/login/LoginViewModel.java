package com.rickstaff.app.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.rickstaff.app.data.model.LoginResponse;
import com.rickstaff.app.data.remote.AuthRepository;

public class LoginViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<LoginResponse> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<LoginResponse> getLoginSuccess() { return loginSuccess; }
    public LiveData<String> getLoginError() { return loginError; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void login(String email, String senha) {
        if (email.isEmpty() || senha.isEmpty()) {
            loginError.postValue("Preencha email e senha");
            return;
        }

        isLoading.postValue(true);

        repository.login(email, senha, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                isLoading.postValue(false);
                loginSuccess.postValue(response);
            }

            @Override
            public void onError(@NonNull String message) {
                isLoading.postValue(false);
                loginError.postValue(message);
            }
        });
    }
}