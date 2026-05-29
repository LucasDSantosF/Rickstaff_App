package com.rickstaff.app.ui.employees;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.rickstaff.app.data.model.Employee;
import com.rickstaff.app.data.remote.EmployeeRepository;
import java.util.List;

public class EmployeeViewModel extends ViewModel {

    private final EmployeeRepository repository = new EmployeeRepository();
    private final MutableLiveData<List<Employee>> employees = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> actionSuccess = new MutableLiveData<>();

    public LiveData<List<Employee>> getEmployees() { return employees; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getActionSuccess() { return actionSuccess; }

    public void loadAll() {
        isLoading.postValue(true);
        repository.getAll(new EmployeeRepository.ListCallback() {
            @Override public void onSuccess(List<Employee> list) {
                isLoading.postValue(false);
                employees.postValue(list);
            }
            @Override public void onError(String msg) {
                isLoading.postValue(false);
                error.postValue(msg);
            }
        });
    }

    public void save(Employee emp) {
        isLoading.postValue(true);
        EmployeeRepository.ActionCallback cb = new EmployeeRepository.ActionCallback() {
            @Override public void onSuccess(Employee e) {
                isLoading.postValue(false);
                actionSuccess.postValue("Salvo com sucesso!");
                loadAll();
            }
            @Override public void onError(String msg) {
                isLoading.postValue(false);
                error.postValue(msg);
            }
        };
        if (emp.getId() == 0) repository.create(emp, cb);
        else repository.update(emp, cb);
    }

    public void delete(int id) {
        repository.delete(id, new EmployeeRepository.DeleteCallback() {
            @Override public void onSuccess() {
                actionSuccess.postValue("Excluído com sucesso!");
                loadAll();
            }
            @Override public void onError(String msg) {
                error.postValue(msg);
            }
        });
    }
}