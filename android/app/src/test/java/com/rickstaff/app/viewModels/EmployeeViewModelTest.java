package com.rickstaff.app.viewModels;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.rickstaff.app.data.model.Employee;
import com.rickstaff.app.data.remote.EmployeeRepository;
import com.rickstaff.app.ui.employees.EmployeeViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private EmployeeRepository mockRepository;
    @Mock private Observer<List<Employee>> employeesObserver;
    @Mock private Observer<Boolean> loadingObserver;
    @Mock private Observer<String> errorObserver;
    @Mock private Observer<String> actionSuccessObserver;

    private EmployeeViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        viewModel = new EmployeeViewModel();
        java.lang.reflect.Field repoField =
                EmployeeViewModel.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(viewModel, mockRepository);

        viewModel.getEmployees().observeForever(employeesObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);
        viewModel.getError().observeForever(errorObserver);
        viewModel.getActionSuccess().observeForever(actionSuccessObserver);
    }

    @Test
    public void loadAll_ativa_loading_antes_de_responder() {
        viewModel.loadAll();
        verify(loadingObserver).onChanged(true);
    }

    @Test
    public void loadAll_sucesso_emite_lista_e_desativa_loading() {
        List<Employee> fakeList = Arrays.asList(new Employee(), new Employee());
        ArgumentCaptor<EmployeeRepository.ListCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ListCallback.class);

        viewModel.loadAll();
        verify(mockRepository).getAll(captor.capture());
        captor.getValue().onSuccess(fakeList);

        verify(employeesObserver).onChanged(fakeList);
        verify(loadingObserver, times(2)).onChanged(false);
    }

    @Test
    public void loadAll_sucesso_nao_emite_erro() {
        ArgumentCaptor<EmployeeRepository.ListCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ListCallback.class);

        viewModel.loadAll();
        verify(mockRepository).getAll(captor.capture());
        captor.getValue().onSuccess(List.of(new Employee()));

        verify(errorObserver, never()).onChanged(anyString());
    }

    @Test
    public void loadAll_erro_emite_mensagem_e_desativa_loading() {
        ArgumentCaptor<EmployeeRepository.ListCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ListCallback.class);

        viewModel.loadAll();
        verify(mockRepository).getAll(captor.capture());
        captor.getValue().onError("Erro ao carregar.");

        verify(errorObserver).onChanged("Erro ao carregar.");
        verify(loadingObserver, times(2)).onChanged(false);
    }

    @Test
    public void loadAll_erro_nao_emite_lista() {
        ArgumentCaptor<EmployeeRepository.ListCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ListCallback.class);

        viewModel.loadAll();
        verify(mockRepository).getAll(captor.capture());
        captor.getValue().onError("Falha.");

        verify(employeesObserver, never()).onChanged(any());
    }

    @Test
    public void save_novo_funcionario_chama_criar_no_repositorio() {
        Employee novo = new Employee();
        viewModel.save(novo);
        verify(mockRepository).create(eq(novo), any());
        verify(mockRepository, never()).update(any(), any());
    }

    @Test
    public void save_novoFuncionario_ativaLoading() {
        viewModel.save(new Employee());
        verify(loadingObserver).onChanged(true);
    }

    @Test
    public void save_criar_sucesso_emite_mensagem_e_recarrega_lista() {
        Employee novo = new Employee();
        ArgumentCaptor<EmployeeRepository.ActionCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ActionCallback.class);

        viewModel.save(novo);
        verify(mockRepository).create(eq(novo), captor.capture());
        captor.getValue().onSuccess(novo);

        verify(actionSuccessObserver).onChanged("Salvo com sucesso!");
        verify(mockRepository).getAll(any());
    }

    @Test
    public void save_criar_sucesso_desativa_loading() {
        Employee novo = new Employee();
        ArgumentCaptor<EmployeeRepository.ActionCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ActionCallback.class);

        viewModel.save(novo);
        verify(mockRepository).create(eq(novo), captor.capture());
        captor.getValue().onSuccess(novo);

        verify(loadingObserver, times(2)).onChanged(false);
    }

    @Test
    public void save_criar_erro_emite_mensagem_e_desativa_loading() {
        Employee novo = new Employee();
        ArgumentCaptor<EmployeeRepository.ActionCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ActionCallback.class);

        viewModel.save(novo);
        verify(mockRepository).create(eq(novo), captor.capture());
        captor.getValue().onError("Falha ao criar.");

        verify(errorObserver).onChanged("Falha ao criar.");
        verify(loadingObserver, times(2)).onChanged(false);
    }

    @Test
    public void save_criar_erro_nao_emite_success_nem_recarrega_lista() {
        Employee novo = new Employee();
        ArgumentCaptor<EmployeeRepository.ActionCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ActionCallback.class);

        viewModel.save(novo);
        verify(mockRepository).create(eq(novo), captor.capture());
        captor.getValue().onError("Erro.");

        verify(actionSuccessObserver, never()).onChanged(anyString());
        verify(mockRepository, never()).getAll(any());
    }

    @Test
    public void save_funcionario_existente_chama_Update_no_repositorio() {
        Employee existente = new Employee();
        existente.setId(42);

        viewModel.save(existente);

        verify(mockRepository).update(eq(existente), any());
        verify(mockRepository, never()).create(any(), any());
    }

    @Test
    public void save_atualizar_sucesso_emite_mensagem_e_recarrega_lista() {
        Employee existente = new Employee();
        existente.setId(42);
        ArgumentCaptor<EmployeeRepository.ActionCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.ActionCallback.class);

        viewModel.save(existente);
        verify(mockRepository).update(eq(existente), captor.capture());
        captor.getValue().onSuccess(existente);

        verify(actionSuccessObserver).onChanged("Salvo com sucesso!");
        verify(mockRepository).getAll(any());
    }

    @Test
    public void delete_chama_delete_no_repositorio_com_id_correto() {
        viewModel.delete(99);
        verify(mockRepository).delete(eq(99), any());
    }

    @Test
    public void delete_sucesso_emite_mensagem_e_recarrega_lista() {
        ArgumentCaptor<EmployeeRepository.DeleteCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.DeleteCallback.class);

        viewModel.delete(99);
        verify(mockRepository).delete(eq(99), captor.capture());
        captor.getValue().onSuccess();

        verify(actionSuccessObserver).onChanged("Excluído com sucesso!");
        verify(mockRepository).getAll(any());
    }

    @Test
    public void delete_sucesso_nao_emite_erro() {
        ArgumentCaptor<EmployeeRepository.DeleteCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.DeleteCallback.class);

        viewModel.delete(99);
        verify(mockRepository).delete(eq(99), captor.capture());
        captor.getValue().onSuccess();

        verify(errorObserver, never()).onChanged(anyString());
    }

    @Test
    public void delete_erro_emite_mensagem() {
        ArgumentCaptor<EmployeeRepository.DeleteCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.DeleteCallback.class);

        viewModel.delete(99);
        verify(mockRepository).delete(eq(99), captor.capture());
        captor.getValue().onError("Não foi possível excluir.");

        verify(errorObserver).onChanged("Não foi possível excluir.");
    }

    @Test
    public void delete_erro_nao_emite_success_nem_recarrega_lista() {
        ArgumentCaptor<EmployeeRepository.DeleteCallback> captor =
                ArgumentCaptor.forClass(EmployeeRepository.DeleteCallback.class);

        viewModel.delete(99);
        verify(mockRepository).delete(eq(99), captor.capture());
        captor.getValue().onError("Erro.");

        verify(actionSuccessObserver, never()).onChanged(anyString());
        verify(mockRepository, never()).getAll(any());
    }

    @Test
    public void estado_inicial_loading_for_false_restante_vazio() {
        verify(loadingObserver).onChanged(false);
        verify(employeesObserver, never()).onChanged(any());
        verify(errorObserver, never()).onChanged(anyString());
        verify(actionSuccessObserver, never()).onChanged(anyString());
    }
}