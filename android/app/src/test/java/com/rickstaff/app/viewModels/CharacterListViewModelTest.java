package com.rickstaff.app.viewModels;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import android.content.Context;

import com.rickstaff.app.data.model.Character;
import com.rickstaff.app.data.model.CharacterResponse;
import com.rickstaff.app.data.model.CharacterResponse.Info;
import com.rickstaff.app.data.remote.CharacterRepository;
import com.rickstaff.app.ui.characters.CharacterListViewModel;
import com.rickstaff.app.util.NetworkUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CharacterListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private CharacterRepository mockRepository;
    @Mock private Context mockContext;
    @Mock private Observer<List<Character>> charactersObserver;
    @Mock private Observer<Boolean> loadingObserver;
    @Mock private Observer<Boolean> emptyObserver;
    @Mock private Observer<String> errorObserver;

    private MockedStatic<NetworkUtils> mockedNetworkUtils;
    private CharacterListViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockedNetworkUtils = mockStatic(NetworkUtils.class);
        mockedNetworkUtils.when(() -> NetworkUtils.isConnected(any())).thenReturn(true);

        viewModel = new CharacterListViewModel();
        java.lang.reflect.Field repoField =
                CharacterListViewModel.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(viewModel, mockRepository);

        viewModel.getCharacters().observeForever(charactersObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);
        viewModel.getIsEmpty().observeForever(emptyObserver);
        viewModel.getError().observeForever(errorObserver);
    }

    @After
    public void tearDown() {
        mockedNetworkUtils.close();
    }

    private CharacterResponse buildResponse(List<Character> results, String nextPage) {
        Info info = mock(Info.class);
        when(info.getNext()).thenReturn(nextPage);

        CharacterResponse response = mock(CharacterResponse.class);
        when(response.getInfo()).thenReturn(info);
        when(response.getResults()).thenReturn(results);
        return response;
    }

    private ArgumentCaptor<CharacterRepository.CharacterCallback> captureCallback(int page) {
        ArgumentCaptor<CharacterRepository.CharacterCallback> captor =
                ArgumentCaptor.forClass(CharacterRepository.CharacterCallback.class);
        verify(mockRepository).getCharacters(eq(page), anyString(), anyString(), anyString(),
                captor.capture());
        return captor;
    }

    @Test
    public void loadFirstPage_sem_conexao_emite_erro_sem_chamar_repositorio() {
        mockedNetworkUtils.when(() -> NetworkUtils.isConnected(any())).thenReturn(false);

        viewModel.loadFirstPage(mockContext);

        verify(errorObserver).onChanged("Sem conexão com a internet");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void loadNextPage_sem_conexao_emite_erro_sem_chamar_repositorio() {
        mockedNetworkUtils.when(() -> NetworkUtils.isConnected(any())).thenReturn(false);

        viewModel.loadNextPage(mockContext);

        verify(errorObserver).onChanged("Sem conexão com a internet");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void loadFirstPage_ativa_loading_e_reseta_lista() {
        viewModel.loadFirstPage(mockContext);

        verify(loadingObserver).onChanged(true);
        verify(charactersObserver, atLeast(2)).onChanged(argThat(List::isEmpty));
    }

    @Test
    public void loadFirstPage_sucesso_emite_personagens_e_desativa_loading() {
        List<Character> fakeList = Arrays.asList(new Character(), new Character());
        CharacterResponse response = buildResponse(fakeList, "page2");

        viewModel.loadFirstPage(mockContext);
        captureCallback(1).getValue().onSuccess(response);

        verify(charactersObserver, atLeastOnce()).onChanged(argThat(list -> list.size() == 2));
        verify(loadingObserver, times(2)).onChanged(false);
    }

    @Test
    public void loadFirstPage_sucesso_emite_IsEmpty_false() {
        CharacterResponse response = buildResponse(List.of(new Character()), "page2");

        viewModel.loadFirstPage(mockContext);
        captureCallback(1).getValue().onSuccess(response);

        verify(emptyObserver, times(2)).onChanged(false);
    }

    @Test
    public void loadFirstPage_sucesso_nao_emite_erro() {
        CharacterResponse response = buildResponse(List.of(new Character()), "page2");

        viewModel.loadFirstPage(mockContext);
        captureCallback(1).getValue().onSuccess(response);

        verify(errorObserver, never()).onChanged(anyString());
    }

    @Test
    public void loadFirstPage_resposta_vazia_emite_IsEmpty_true() {
        CharacterResponse response = buildResponse(new ArrayList<>(), null);

        viewModel.loadFirstPage(mockContext);
        captureCallback(1).getValue().onSuccess(response);

        verify(emptyObserver).onChanged(true);
    }

    @Test
    public void loadFirstPage_erro_lista_vazia_emite_mensagem_e_IsEmpty_true() {
        viewModel.loadFirstPage(mockContext);
        captureCallback(1).getValue().onError("Personagem não encontrado.");

        verify(errorObserver).onChanged("Personagem não encontrado.");
        verify(emptyObserver).onChanged(true);
    }

    @Test
    public void loadFirstPage_erro_apos_reset_emite_erro_e_IsEmpty_true() {
        CharacterResponse first = buildResponse(List.of(new Character()), "page2");
        viewModel.loadFirstPage(mockContext);

        ArgumentCaptor<CharacterRepository.CharacterCallback> captor =
                ArgumentCaptor.forClass(CharacterRepository.CharacterCallback.class);
        verify(mockRepository, times(1)).getCharacters(eq(1), anyString(), anyString(),
                anyString(), captor.capture());
        captor.getValue().onSuccess(first);

        viewModel.loadFirstPage(mockContext);

        ArgumentCaptor<CharacterRepository.CharacterCallback> captor2 =
                ArgumentCaptor.forClass(CharacterRepository.CharacterCallback.class);
        verify(mockRepository, times(2)).getCharacters(eq(1), anyString(), anyString(),
                anyString(), captor2.capture());
        captor2.getAllValues().get(1).onError("Falha temporária.");

        verify(errorObserver).onChanged("Falha temporária.");
        verify(emptyObserver).onChanged(true);
    }

    @Test
    public void loadNextPage_sem_pagina_anterior_nao_carrega() {
        viewModel.loadNextPage(mockContext);
        verify(mockRepository).getCharacters(eq(2), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void loadNextPage_isLastPage_nao_chama() {
        CharacterResponse response = buildResponse(List.of(new Character()), null);
        viewModel.loadFirstPage(mockContext);
        captureCallback(1).getValue().onSuccess(response);

        viewModel.loadNextPage(mockContext);

        verify(mockRepository, times(1)).getCharacters(anyInt(), anyString(), anyString(),
                anyString(), any());
    }

    @Test
    public void loadNextPage_em_carregamento_nao_faz_nova_chamada() {
        viewModel.loadFirstPage(mockContext);
        viewModel.loadNextPage(mockContext);

        verify(mockRepository, times(1)).getCharacters(anyInt(), anyString(), anyString(),
                anyString(), any());
    }

    @Test
    public void loadNextPage_sucesso_acumula_personagens_na_lista() {
        List<Character> page1 = Arrays.asList(new Character(), new Character());
        CharacterResponse resp1 = buildResponse(page1, "page2");
        viewModel.loadFirstPage(mockContext);
        captureCallback(1).getValue().onSuccess(resp1);

        List<Character> page2 = List.of(new Character());
        CharacterResponse resp2 = buildResponse(page2, null);
        viewModel.loadNextPage(mockContext);
        captureCallback(2).getValue().onSuccess(resp2);

        verify(charactersObserver, atLeastOnce()).onChanged(argThat(list -> list.size() == 3));
    }

    @Test
    public void applyFilters_reseta_lista_e_carrega_pagina_um_com_filtros() {
        viewModel.applyFilters(mockContext, "alive", "male", "human");

        ArgumentCaptor<CharacterRepository.CharacterCallback> captor =
                ArgumentCaptor.forClass(CharacterRepository.CharacterCallback.class);
        verify(mockRepository).getCharacters(eq(1), eq("alive"), eq("male"), eq("human"),
                captor.capture());
    }

    @Test
    public void applyFilters_sem_conexao_emite_erro_sem_chamar_repositorio() {
        mockedNetworkUtils.when(() -> NetworkUtils.isConnected(any())).thenReturn(false);

        viewModel.applyFilters(mockContext, "alive", "male", "human");

        verify(errorObserver).onChanged("Sem conexão com a internet");
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void estado_inicial_valores_corretos() {
        verify(loadingObserver).onChanged(false);
        verify(emptyObserver).onChanged(false);
        verify(charactersObserver).onChanged(argThat(List::isEmpty));
        verify(errorObserver, never()).onChanged(anyString());
    }
}