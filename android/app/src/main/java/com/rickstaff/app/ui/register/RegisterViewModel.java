package com.rickstaff.app.ui.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rickstaff.app.data.model.LoginResponse;
import com.rickstaff.app.data.remote.AuthRepository;

public class RegisterViewModel extends ViewModel {
    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<LoginResponse> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<LoginResponse> getRegisterSuccess() { return registerSuccess; }
    public LiveData<String> getRegisterError() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }


    public void registerUser(String name, String email, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Preencha todos os campos.");
            return;
        }

        isLoading.setValue(true);

        repository.register(name, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                isLoading.postValue(false);
                registerSuccess.postValue(response);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }
}
