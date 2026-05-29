package com.rickstaff.app.viewModels;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.rickstaff.app.data.model.LoginResponse;
import com.rickstaff.app.data.remote.AuthRepository;
import com.rickstaff.app.ui.login.LoginViewModel;

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
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class LoginViewModelTest {

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
    private LoginViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        viewModel = new LoginViewModel();
        java.lang.reflect.Field repoField =
                LoginViewModel.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(viewModel, mockRepository);

        viewModel.getLoginSuccess().observeForever(successObserver);
        viewModel.getLoginError().observeForever(errorObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);
    }

    @Test
    public void login_campos_vazios_emite_erro_sem_chamar_repositorio() {
        viewModel.login("", "");

        verify(errorObserver).onChanged("Preencha email e senha");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void login_email_vazio_emite_erro() {
        viewModel.login("", "senha123");

        verify(errorObserver).onChanged("Preencha email e senha");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void login_senha_vazia_emite_erro() {
        viewModel.login("user@email.com", "");

        verify(errorObserver).onChanged("Preencha email e senha");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void login_dados_validos_ativa_loading_antes_de_responder() {
        viewModel.login("user@email.com", "senha123");

        verify(loadingObserver).onChanged(true);
    }

    @Test
    public void login_campos_vazios_nao_ativa_loading() {
        viewModel.login("", "");

        verify(loadingObserver, never()).onChanged(true);
    }

    @Test
    public void login_sucesso_emite_login_response_e_desativa_loading() {
        LoginResponse fakeResponse = new LoginResponse();
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        viewModel.login("user@email.com", "senha123");

        verify(mockRepository).login(
                eq("user@email.com"),
                eq("senha123"),
                callbackCaptor.capture()
        );

        callbackCaptor.getValue().onSuccess(fakeResponse);

        verify(successObserver).onChanged(fakeResponse);
        verify(loadingObserver, times(2)).onChanged(false);
    }

    @Test
    public void login_sucesso_nao_emite_mensagem_de_erro() {
        LoginResponse fakeResponse = new LoginResponse();
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        viewModel.login("user@email.com", "senha123");
        verify(mockRepository).login(anyString(), anyString(), callbackCaptor.capture());

        callbackCaptor.getValue().onSuccess(fakeResponse);

        verify(errorObserver, never()).onChanged(anyString());
    }

    @Test
    public void login_erro_emite_mensagem_de_erro_e_desativa_loading() {
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        viewModel.login("user@email.com", "senha123");
        verify(mockRepository).login(anyString(), anyString(), callbackCaptor.capture());

        callbackCaptor.getValue().onError("Credenciais inválidas.");

        verify(errorObserver).onChanged("Credenciais inválidas.");
        verify(loadingObserver, times(2)).onChanged(false);
    }

    @Test
    public void login_erro_nao_emite_success_Observer() {
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        viewModel.login("user@email.com", "senha123");
        verify(mockRepository).login(anyString(), anyString(), callbackCaptor.capture());

        callbackCaptor.getValue().onError("Erro de rede.");

        verify(successObserver, never()).onChanged(any());
    }

    @Test
    public void liveData_estado_inicial_loading_for_false() {
        verify(loadingObserver).onChanged(false);
        verify(successObserver, never()).onChanged(any());
        verify(errorObserver, never()).onChanged(anyString());
    }
}