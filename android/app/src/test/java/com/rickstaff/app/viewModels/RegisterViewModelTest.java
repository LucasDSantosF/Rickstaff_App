package com.rickstaff.app.viewModels;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.rickstaff.app.data.model.LoginResponse;
import com.rickstaff.app.data.remote.AuthRepository;
import com.rickstaff.app.ui.register.RegisterViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RegisterViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    @Mock
    private AuthRepository mockRepository;
    @Mock
    private Observer<LoginResponse> successObserver;
    @Mock
    private Observer<String> errorObserver;
    @Mock
    private Observer<Boolean> loadingObserver;
    private RegisterViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        viewModel = new RegisterViewModel();
        java.lang.reflect.Field repoField =
                RegisterViewModel.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(viewModel, mockRepository);

        viewModel.getRegisterSuccess().observeForever(successObserver);
        viewModel.getRegisterError().observeForever(errorObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);
    }

    @Test
    public void registerUser_campos_vazios_emite_erro_sem_chamar_repositorio() {
        viewModel.registerUser("", "", "");

        verify(errorObserver).onChanged("Preencha todos os campos.");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void registerUser_nome_vazio_emite_erro() {
        viewModel.registerUser("", "user@email.com", "senha123");

        verify(errorObserver).onChanged("Preencha todos os campos.");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void registerUser_email_vazio_emite_erro() {
        viewModel.registerUser("Nome", "", "senha123");

        verify(errorObserver).onChanged("Preencha todos os campos.");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void registerUser_senha_vazia_emite_erro() {
        viewModel.registerUser("Nome", "user@email.com", "");

        verify(errorObserver).onChanged("Preencha todos os campos.");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void registerUser_dados_validos_ativa_loading_antes_de_responder() {
        viewModel.registerUser("Nome", "user@email.com", "senha123");
        verify(loadingObserver).onChanged(true);
    }

    @Test
    public void registerUser_campos_vazios_nao_ativa_loading() {
        viewModel.registerUser("", "", "");

        verify(loadingObserver, never()).onChanged(true);
    }

    @Test
    public void registerUser_sucesso_emite_loginResponse_e_desativa_loading() {
        LoginResponse fakeResponse = new LoginResponse();
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        viewModel.registerUser("Nome", "user@email.com", "senha123");

        verify(mockRepository).register(
                eq("Nome"),
                eq("user@email.com"),
                eq("senha123"),
                callbackCaptor.capture()
        );

        callbackCaptor.getValue().onSuccess(fakeResponse);

        verify(successObserver).onChanged(fakeResponse);
        verify(loadingObserver).onChanged(false);
    }

    @Test
    public void registerUser_sucesso_nao_emite_mensagem_de_erro() {
        LoginResponse fakeResponse = new LoginResponse();
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        viewModel.registerUser("Nome", "user@email.com", "senha123");
        verify(mockRepository).register(anyString(), anyString(), anyString(),
                callbackCaptor.capture());

        callbackCaptor.getValue().onSuccess(fakeResponse);

        verify(errorObserver, never()).onChanged(anyString());
    }

    @Test
    public void registerUser_erro_emite_mensagem_de_erro_e_desativa_Loading() {
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        viewModel.registerUser("Nome", "user@email.com", "senha123");
        verify(mockRepository).register(anyString(), anyString(), anyString(),
                callbackCaptor.capture());

        callbackCaptor.getValue().onError("E-mail já cadastrado.");

        verify(errorObserver).onChanged("E-mail já cadastrado.");
        verify(loadingObserver).onChanged(false);
    }

    @Test
    public void registerUser_erro_nao_emite_successObserver() {
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        viewModel.registerUser("Nome", "user@email.com", "senha123");
        verify(mockRepository).register(anyString(), anyString(), anyString(),
                callbackCaptor.capture());

        callbackCaptor.getValue().onError("Erro de rede.");

        verify(successObserver, never()).onChanged(any());
    }

    @Test
    public void liveData_estado_inicial_nao_emite_nada() {
        verifyNoInteractions(successObserver);
        verifyNoInteractions(errorObserver);
        verifyNoInteractions(loadingObserver);
    }
}